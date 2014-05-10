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
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
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

    private ChannelHandlerContext ctx;
    private static final MessagingException HANDSHAKE_TIMED_OUT = new MessagingException("Handshake timed out");
    private static final MessagingException CHANNEL_CLOSED = new MessagingException("channel closed");
    private final LazyChannelPromise handshakePromise = new LazyChannelPromise();
    private boolean inErrorAndClosing;

    protected AbstractHandshakeHandler(boolean isClient, long handshakeTimeout) {
        this.client = isClient;
        this.handshakeTimeout = handshakeTimeout;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;

        if (ctx.channel().isActive()) {
            handshake();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshake();
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // connection has closed during handshake
        notifyHandshakeFailure(CHANNEL_CLOSED);
        super.channelInactive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (canRead(client, in)) {
            if (client) {
                decodeAsClient(ctx, in);
            } else {
                decodeAsServer(ctx, in);
            }
        }
    }

    protected abstract boolean canRead(boolean client, ByteBuf in);
    protected abstract void clientWrite(ByteBuf buffer);
    protected abstract void serverWrite(ByteBuf buffer);
    protected abstract boolean serverReadAndValidate(ChannelHandlerContext ctx, ByteBuf in);
    protected abstract boolean clientReadAndValidate(ChannelHandlerContext ctx, ByteBuf in);

    private Future<Channel> handshake() {
        final ScheduledFuture<?> timeoutFuture;
        if (handshakeTimeout > 0) {
            timeoutFuture = ctx.executor().schedule( new Runnable() {
                @Override
                public void run() {
                    if (handshakePromise.isDone()) {
                        return;
                    }
                    notifyHandshakeFailure(HANDSHAKE_TIMED_OUT);
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
            beginHandshake();
        } catch (Exception e) {
            notifyHandshakeFailure(e);
        }

        return handshakePromise;
    }

    private void beginHandshake() {
        if (client) {
            final ByteBuf buffer = ctx.alloc().buffer(BUFFER_SIZE);
            clientWrite(buffer);
            ctx.writeAndFlush(buffer);
        }
    }


    private void decodeAsServer(ChannelHandlerContext ctx, ByteBuf in) {
        if (serverReadAndValidate(ctx, in)) {
            final ByteBuf buffer = ctx.alloc().buffer(BUFFER_SIZE);
            serverWrite(buffer);
            ctx.writeAndFlush(buffer);
            notifyHandshakeSuccess();
        } else {
            notifyHandshakeFailure(new MessagingException("Invalid Header"));
        }
    }




    private void decodeAsClient(ChannelHandlerContext ctx, ByteBuf in) {
        if (clientReadAndValidate(ctx, in)) {
            notifyHandshakeSuccess();
        } else {
            notifyHandshakeFailure(new MessagingException("Invalid Header"));
        }
    }

    private void notifyHandshakeSuccess() {
        removeFromPipeline();

        try {
            handshakePromise.setSuccess(ctx.channel());
            ctx.fireUserEventTriggered(HandshakeCompletionEvent.SUCCESS);
            LOGGER.info("Success");
        } catch (IllegalStateException e) {
            LOGGER.debug("Unable to set success", e);
            ctx.fireUserEventTriggered(new HandshakeCompletionEvent(e));
            ctx.close();
        }
    }

    private void removeFromPipeline() {
        LOGGER.debug("Handshake done removing from pipeline");
        ctx.pipeline().remove(this);
    }

    private void notifyHandshakeFailure(Throwable cause) {
        removeFromPipeline();

        if (!inErrorAndClosing) {
            try {
                inErrorAndClosing = true;
                handshakePromise.setFailure(cause);
            } finally {
                ctx.fireUserEventTriggered(new HandshakeCompletionEvent(cause));
                ctx.close();
                LOGGER.info("Failure in handshake", cause);
            }
        }
    }

    private final class LazyChannelPromise extends DefaultPromise<Channel> {
        @Override
        protected EventExecutor executor() {
            if (ctx == null) {
                throw new IllegalStateException();
            }
            return ctx.executor();
        }
    }

}
