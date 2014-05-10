/**
 * Copyright 2014 Zaradai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zaradai.distributor.messaging.netty.handler;

import com.zaradai.distributor.messaging.MessagingException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHandshakeHandler extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHandshakeHandler.class);
    private static final int BUFFER_SIZE = 64;

    private final boolean client;
    private final long handshakeTimeout;

    private static final MessagingException HANDSHAKE_TIMED_OUT = new MessagingException("Handshake timed out");
    private static final MessagingException CHANNEL_CLOSED = new MessagingException("channel closed");
    private DefaultPromise<Channel> handshakePromise;
    private boolean inErrorAndClosing;

    protected AbstractHandshakeHandler(boolean isClient, long handshakeTimeout) {
        this.client = isClient;
        this.handshakeTimeout = handshakeTimeout;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext handlerContext) throws Exception {
        if (handlerContext.channel().isActive()) {
            handshake(handlerContext);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext handlerContext) throws Exception {
        handshake(handlerContext);
        handlerContext.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext handlerContext) throws Exception {
        // connection has closed during handshake
        notifyHandshakeFailure(handlerContext, CHANNEL_CLOSED);
        super.channelInactive(handlerContext);
    }

    @Override
    protected void decode(ChannelHandlerContext handlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (canRead(client, in)) {
            if (client) {
                decodeAsClient(handlerContext, in);
            } else {
                decodeAsServer(handlerContext, in);
            }
        }
    }

    protected abstract boolean canRead(boolean isClient, ByteBuf in);
    protected abstract void clientWrite(ByteBuf buffer);
    protected abstract void serverWrite(ByteBuf buffer);
    protected abstract boolean serverReadAndValidate(ChannelHandlerContext ctx, ByteBuf in);
    protected abstract boolean clientReadAndValidate(ChannelHandlerContext ctx, ByteBuf in);

    private Future<Channel> handshake(final ChannelHandlerContext handlerContext) {
        handshakePromise = new DefaultPromise<Channel>(handlerContext.executor());
        final ScheduledFuture<?> timeoutFuture;
        if (handshakeTimeout > 0) {
            timeoutFuture = handlerContext.executor().schedule(new Runnable() {
                @Override
                public void run() {
                    if (handshakePromise.isDone()) {
                        return;
                    }
                    notifyHandshakeFailure(handlerContext, HANDSHAKE_TIMED_OUT);
                }
            }, handshakeTimeout, TimeUnit.MILLISECONDS);
        } else {
            timeoutFuture = null;
        }

        handshakePromise.addListener(new GenericFutureListener<Future<? super Channel>>() {
            @Override
            public void operationComplete(Future<? super Channel> future) throws Exception {
                if (timeoutFuture != null) {
                    timeoutFuture.cancel(false);
                }
            }
        });

        try {
            beginHandshake(handlerContext);
        } catch (Exception e) {
            notifyHandshakeFailure(handlerContext, e);
        }

        return handshakePromise;
    }

    private void beginHandshake(ChannelHandlerContext handlerContext) {
        if (client) {
            final ByteBuf buffer = handlerContext.alloc().buffer(BUFFER_SIZE);
            clientWrite(buffer);
            handlerContext.writeAndFlush(buffer);
        }
    }


    private void decodeAsServer(ChannelHandlerContext handlerContext, ByteBuf in) {
        if (serverReadAndValidate(handlerContext, in)) {
            final ByteBuf buffer = handlerContext.alloc().buffer(BUFFER_SIZE);
            serverWrite(buffer);
            handlerContext.writeAndFlush(buffer);
            notifyHandshakeSuccess(handlerContext);
        } else {
            notifyHandshakeFailure(handlerContext, new MessagingException("Invalid Header"));
        }
    }




    private void decodeAsClient(ChannelHandlerContext handlerContext, ByteBuf in) {
        if (clientReadAndValidate(handlerContext, in)) {
            notifyHandshakeSuccess(handlerContext);
        } else {
            notifyHandshakeFailure(handlerContext, new MessagingException("Invalid Header"));
        }
    }

    private void notifyHandshakeSuccess(ChannelHandlerContext handlerContext) {
        removeFromPipeline(handlerContext);

        try {
            handshakePromise.setSuccess(handlerContext.channel());
            handlerContext.fireUserEventTriggered(HandshakeCompletionEvent.SUCCESS);
            LOGGER.info("Success");
        } catch (IllegalStateException e) {
            LOGGER.debug("Unable to set success", e);
            handlerContext.fireUserEventTriggered(new HandshakeCompletionEvent(e));
            handlerContext.close();
        }
    }

    private void removeFromPipeline(ChannelHandlerContext handlerContext) {
        LOGGER.debug("Handshake done removing from pipeline");
        handlerContext.pipeline().remove(this);
    }

    private void notifyHandshakeFailure(ChannelHandlerContext handlerContext, Throwable cause) {
        removeFromPipeline(handlerContext);

        if (!inErrorAndClosing) {
            try {
                inErrorAndClosing = true;
                handshakePromise.setFailure(cause);
            } finally {
                handlerContext.fireUserEventTriggered(new HandshakeCompletionEvent(cause));
                handlerContext.close();
                LOGGER.info("Failure in handshake", cause);
            }
        }
    }
}
