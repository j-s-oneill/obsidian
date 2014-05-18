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
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.messaging.netty.handler.HandshakeHandlerFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageDecoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageEncoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageHandlerFactory;
import com.zaradai.net.retry.RetryPolicy;
import com.zaradai.net.retry.RetryPolicyBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    private final DistributorConfig config;
    private final EventLoopGroups eventLoopGroups;
    private final MessageEncoderFactory messageEncoderFactory;
    private final MessageDecoderFactory messageDecoderFactory;
    private final MessageHandlerFactory messageHandlerFactory;
    private final HandshakeHandlerFactory handshakeHandlerFactory;
    private final InetSocketAddress endpoint;
    private final Bootstrap bootstrap;

    @Inject
    NettyClient(
            DistributorConfig config,
            EventLoopGroups eventLoopGroups,
            MessageEncoderFactory messageEncoderFactory,
            MessageDecoderFactory messageDecoderFactory,
            MessageHandlerFactory messageHandlerFactory,
            HandshakeHandlerFactory handshakeHandlerFactory,
            @Assisted InetSocketAddress endpoint) {
        this.config = config;
        this.eventLoopGroups = eventLoopGroups;
        this.messageEncoderFactory = messageEncoderFactory;
        this.messageDecoderFactory = messageDecoderFactory;
        this.messageHandlerFactory = messageHandlerFactory;
        this.handshakeHandlerFactory = handshakeHandlerFactory;
        this.endpoint = endpoint;
        bootstrap = createBootstrap();
    }

    public void connect() {
        LOGGER.info("Connecting to {}", endpoint);
        connect(new RetryPolicyBuilder(
                config.getRetryAttempts(),
                config.getRetryDelay(),
                config.getRetryMaxDelay(),
                config.getRetryUseExponentialBackOff())
                .build()
        );
    }

    private void connect(final RetryPolicy retryPolicy) {
        ChannelFuture future = bootstrap.connect(endpoint);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                final ChannelFuture fut = channelFuture;

                if (channelFuture.isSuccess()) {
                    connected();
                } else {
                    eventLoopGroups.getClientGroup().submit(new Runnable() {
                        @Override
                        public void run() {
                            if (retryPolicy.retry()) {
                                connect(retryPolicy);
                            } else {
                                failed(fut.cause());
                            }
                        }
                    });
                }
            }
        });
    }

    private void failed(Throwable cause) {
        // not needed remove after dev
        LOGGER.warn("Unable to connect to {}", endpoint, cause);
    }

    private void connected() {
        // not needed remove after dev
        LOGGER.info("Connected to {}", endpoint);
    }

    private Bootstrap createBootstrap() {
        Bootstrap res = new Bootstrap().group(eventLoopGroups.getClientGroup()).channel(NioSocketChannel.class);
        configure(res);
        res.handler(createClientInitializer());

        return res;
    }

    private ChannelInitializer<SocketChannel> createClientInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (config.getVerboseLogging()) {
                    pipeline.addLast(new LoggingHandler("CLIENT"));
                }
                pipeline.addLast("handshake", handshakeHandlerFactory.create(true));
                pipeline.addLast("decoder", messageDecoderFactory.create());
                pipeline.addLast("encoder", messageEncoderFactory.create());
                pipeline.addLast("handler", messageHandlerFactory.create());
            }
        };
    }

    private void configure(Bootstrap b) {
        b.option(ChannelOption.TCP_NODELAY, config.getTcpNoDelay());
        b.option(ChannelOption.SO_KEEPALIVE, config.getKeepAlive());
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout());
    }
}
