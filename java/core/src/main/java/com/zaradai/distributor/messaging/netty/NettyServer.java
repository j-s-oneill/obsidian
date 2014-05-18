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
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.messaging.MessagingException;
import com.zaradai.distributor.messaging.netty.handler.HandshakeHandlerFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageDecoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageEncoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private final DistributorConfig config;
    private final EventLoopGroups eventLoopGroups;
    private final MessageEncoderFactory messageEncoderFactory;
    private final MessageDecoderFactory messageDecoderFactory;
    private final MessageHandlerFactory messageHandlerFactory;
    private final HandshakeHandlerFactory handshakeHandlerFactory;
    private final DefaultChannelGroup serverChannelGroup;
    private final ServerBootstrap bootstrap;

    private final ChannelFutureListener bound = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            bindComplete(channelFuture);
        }
    };

    @Inject
    NettyServer(DistributorConfig config,
                EventLoopGroups eventLoopGroups,
                MessageEncoderFactory messageEncoderFactory,
                MessageDecoderFactory messageDecoderFactory,
                MessageHandlerFactory messageHandlerFactory,
                HandshakeHandlerFactory handshakeHandlerFactory) {
        this.config = config;
        this.eventLoopGroups = eventLoopGroups;
        this.messageEncoderFactory = messageEncoderFactory;
        this.messageDecoderFactory = messageDecoderFactory;
        this.messageHandlerFactory = messageHandlerFactory;
        this.handshakeHandlerFactory = handshakeHandlerFactory;
        serverChannelGroup = new DefaultChannelGroup("Server Accept Channels", GlobalEventExecutor.INSTANCE);
        bootstrap = createBootstrap();
    }

    public void listen() throws MessagingException {
        bootstrap.bind(getBindAddress(), config.getPort()).addListener(bound);
    }

    public void shutdown() throws MessagingException {
        try {
            serverChannelGroup.close().await();
        } catch (InterruptedException e) {
            throw new MessagingException("Error closing server", e);
        }
    }

    private ServerBootstrap createBootstrap() {
        ServerBootstrap res = new ServerBootstrap()
                .group(eventLoopGroups.getServerGroup(), eventLoopGroups.getClientGroup())
                .channel(NioServerSocketChannel.class);
        configure(res);
        res.childHandler(createClientInitializer());

        return res;
    }

    private ChannelInitializer<SocketChannel> createClientInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (config.getVerboseLogging()) {
                    pipeline.addLast(new LoggingHandler("SERVER-CLIENT"));
                }
                pipeline.addLast("handshake", handshakeHandlerFactory.create(false));
                pipeline.addLast("decoder", messageDecoderFactory.create());
                pipeline.addLast("encoder", messageEncoderFactory.create());
                pipeline.addLast("handler", messageHandlerFactory.create());
            }
        };
    }

    private void configure(ServerBootstrap b) {
        b.option(ChannelOption.SO_BACKLOG, config.getAcceptBacklog());
        b.option(ChannelOption.SO_REUSEADDR, config.getReuseAddress());
        b.childOption(ChannelOption.TCP_NODELAY, config.getTcpNoDelay());
        b.childOption(ChannelOption.SO_KEEPALIVE, config.getKeepAlive());
    }

    private void bindComplete(ChannelFuture channelFuture) {
        if (channelFuture.isSuccess()) {
            LOGGER.info("Listening on {}", channelFuture.channel().localAddress());
            serverChannelGroup.add(channelFuture.channel());
        } else {
            LOGGER.warn("Unable to listen", channelFuture.cause());
        }
    }

    private InetAddress getBindAddress() throws MessagingException {
        String listenOn = config.getHost();

        try {
            return InetAddress.getByName(listenOn);
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get address for host {}, using local", listenOn, e);
            return getLocalHost();
        }
    }

    private InetAddress getLocalHost() throws MessagingException {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get localhost, using 0.0.0.0", e);
            try {
                return InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException e1) {
                // give up
                LOGGER.warn("Unable to get host 0.0.0.0, aborting listen", e1);
                throw new MessagingException("Unable to obtain listen host address");
            }
        }
    }
}
