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

import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.messaging.MessagingException;
import com.zaradai.distributor.messaging.Server;
import com.zaradai.distributor.messaging.netty.handler.ConnectionAuthenticatorHandler;
import com.zaradai.distributor.messaging.netty.handler.InitializerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NettyServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final DistributorConfig config;
    private final EventLoopGroups eventLoopGroups;
    private final InitializerFactory initializerFactory;
    private final ConnectionAuthenticatorHandler connectionAuthenticatorHandler;
    private final DefaultChannelGroup serverChannelGroup;
    private final ServerBootstrap bootstrap;

    private final ChannelFutureListener bound = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            bindComplete(channelFuture);
        }
    };

    public NettyServer(
            DistributorConfig config,
            EventLoopGroups eventLoopGroups,
            InitializerFactory initializerFactory,
            ConnectionAuthenticatorHandler connectionAuthenticatorHandler) {
        this.config = config;
        this.eventLoopGroups = eventLoopGroups;
        this.initializerFactory = initializerFactory;
        this.connectionAuthenticatorHandler = connectionAuthenticatorHandler;
        serverChannelGroup = new DefaultChannelGroup("Server Accept Channels", GlobalEventExecutor.INSTANCE);
        bootstrap = createBootstrap();
    }

    @Override
    public void listen() throws MessagingException {
        bootstrap.bind(getBindAddress(), config.getPort()).addListener(bound);
    }

    @Override
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
        res.handler(createAcceptorInitializer());
        res.childHandler(initializerFactory.create(false));

        return res;
    }

    private ChannelInitializer<ServerSocketChannel> createAcceptorInitializer() {
        return new ChannelInitializer<ServerSocketChannel>() {
            @Override
            protected void initChannel(ServerSocketChannel channel) throws Exception {
                channel.pipeline().addLast(connectionAuthenticatorHandler);
                channel.pipeline().addLast(new LoggingHandler("ACCEPTOR"));  // remove once dev complete
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
