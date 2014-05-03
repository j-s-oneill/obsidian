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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.events.NodeConnectedEvent;
import com.zaradai.distributor.events.NodeDisconnectedEvent;
import com.zaradai.distributor.messaging.Connection;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.netty.NettyConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
    private final DistributorConfig config;
    private final ConnectionManager connectionManager;
    private final Boolean clientHandler;
    private final EventPublisher eventPublisher;

    @Inject
    MessageHandler(EventPublisher eventPublisher, DistributorConfig config, ConnectionManager connectionManager,
                   @Assisted Boolean clientHandler) {
        this.eventPublisher = eventPublisher;
        this.config = config;
        this.connectionManager = connectionManager;
        this.clientHandler = clientHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConnection connection = (NettyConnection) getConnection(ctx.channel());

        if (connection != null) {
            connection.setChannel(ctx.channel());
        }
        // notify of channel activation
        publishNodeConnected(ctx.channel());

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyConnection connection = (NettyConnection) getConnection(ctx.channel());

        if (connection != null) {
            connection.setChannel(null);
        }
        // notify of channel deactivation
        publishNodeDisconnected(ctx.channel());

        super.channelInactive(ctx);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        if (clientHandler) {
            publish(message);
        }
    }

    private Connection getConnection(Channel channel) {
        Connection res = null;
        // get the connection for the address
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        // only add if the remote address port is not listening port.
        if (address.getPort() != config.getPort()) {
            res = connectionManager.get(address);
        }

        return res;
    }

    private void publishNodeDisconnected(Channel channel) {
        NodeConnectedEvent event = new NodeConnectedEvent((InetSocketAddress) channel.remoteAddress());
        publish(event);
    }

    private void publishNodeConnected(Channel channel) {
        NodeDisconnectedEvent event = new NodeDisconnectedEvent((InetSocketAddress) channel.remoteAddress());
        publish(event);
    }

    private void publish(final Object event) {
        eventPublisher.publish(event);
    }
}
