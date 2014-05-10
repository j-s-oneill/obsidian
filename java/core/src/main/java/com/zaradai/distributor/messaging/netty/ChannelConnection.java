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
package com.zaradai.distributor.messaging.netty;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.events.MessageSentEvent;
import com.zaradai.distributor.messaging.AbstractPendingConnection;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelConnection extends AbstractPendingConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelConnection.class);

    private final NettyClientFactory nettyClientFactory;
    private final EventPublisher eventPublisher;
    private volatile Channel channel;
    private final InetSocketAddress endpoint;
    private AtomicBoolean doReconnect;
    private final ChannelFutureListener lostNotifier = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            setChannel(null);
        }
    };

    @Inject
    ChannelConnection(EventPublisher eventPublisher, NettyClientFactory nettyClientFactory,
                      @Assisted InetSocketAddress endpoint) {
        this.eventPublisher = eventPublisher;
        this.nettyClientFactory = nettyClientFactory;
        this.endpoint = endpoint;
        doReconnect = new AtomicBoolean(true);
    }

    public void setChannel(Channel channel) {
        logActivity(channel);
        removeCloseListener();
        this.channel = channel;
        addCloseListener();

        if (channel != null) {
            // after a successful connection we will wish to reconnect if dropped
            doReconnect.set(true);
            // flush any pending messages
            flushPending();
        } else {
            // try to reconnect we lost the connection
            connect();
        }
    }

    private void logActivity(Channel toSet) {
        if (toSet != null) {
            LOGGER.info("Activating: {}", endpoint);
        } else {
            LOGGER.info("Deactivating: {}", endpoint);
        }
    }

    private void addCloseListener() {
        if (channel != null) {
            channel.closeFuture().addListener(lostNotifier);
        }
    }

    private void removeCloseListener() {
        if (channel != null) {
            channel.closeFuture().removeListener(lostNotifier);
        }
    }


    @Override
    protected void connect() {
        if (doReconnect.get()) {
            // stop repeated attempts
            doReconnect.set(false);
            // getOrCreate a client to initiate the connection
            nettyClientFactory.create(endpoint).connect();
        }
    }

    @Override
    protected void doSend(final Message message) throws MessagingException {
        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    onSuccess(message);
                } else {
                    onFailure(message, future.cause());
                }
            }
        });
    }

    @Override
    protected boolean isConnected() {
        return channel != null;
    }

    @Override
    public void shutdown() {
        if (channel != null) {
            channel.close();
        }
    }

    protected void onSuccess(Message message) {
        eventPublisher.publish(new MessageSentEvent(message));
    }

    protected void onFailure(Message message, Throwable cause) {
        eventPublisher.publish(new MessageErrorEvent(message, cause.getMessage()));
    }

    private void flushPending() {
        final List<Message> drainedMessages = drainPending();

        for (Message message : drainedMessages) {
            try {
                send(message);
            } catch (MessagingException e) {
                // notify of send error
                eventPublisher.publish(new MessageErrorEvent(message, e.getMessage()));
            }
        }
    }
}
