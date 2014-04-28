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
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.events.MessageSentEvent;
import com.zaradai.distributor.messaging.AbstractPendingCacheConnection;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingException;
import com.zaradai.events.EventAggregator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyConnection extends AbstractPendingCacheConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);
    private final EventAggregator eventAggregator;
    private volatile Channel channel;
    private final ChannelFutureListener channelCloseHandler = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            setChannel(null);
        }
    };

    @Inject
    NettyConnection(EventAggregator eventAggregator) {
        this.eventAggregator = eventAggregator;
    }

    @Override
    protected void doSend(final Message message) throws MessagingException {
        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    //post a sent message
                    eventAggregator.publish(new MessageSentEvent(message));
                } else {
                    // post a failure message
                    eventAggregator.publish(new MessageErrorEvent(message, channelFuture.cause().getMessage()));
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
        if (isConnected()) {
            channel.close();
        }
    }

    public void setChannel(Channel channel) {
        removeCloseListener();

        this.channel = channel;

        addCloseListener();
    }

    private void addCloseListener() {
        if (this.channel != null) {
            this.channel.closeFuture().addListener(channelCloseHandler);
            try {
                flushPendingMessages();
            } catch (MessagingException e) {
                // don't like this state, rethink design
                LOGGER.error("Unable to send", e);
            }
        }
    }

    private void removeCloseListener() {
        if (this.channel != null) {
            this.channel.closeFuture().removeListener(channelCloseHandler);
        }
    }
}
