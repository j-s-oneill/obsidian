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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;


public class Initializer extends ChannelInitializer<SocketChannel> {
    static final String DECODER_NAME = "decoder";
    static final String ENCODER_NAME = "encoder";
    static final String HANDLER_NAME = "handler";
    static final String CLIENT_LOGGER_NAME = "CLIENT";
    static final String SERVER_LOGGER_NAME = "SERVER";

    private final MessageDecoderFactory messageDecoderFactory;
    private final MessageEncoderFactory messageEncoderFactory;
    private final MessageHandlerFactory messageHandlerFactory;
    private final Boolean clientInitializer;

    @Inject
    Initializer(MessageDecoderFactory messageDecoderFactory,
                MessageEncoderFactory messageEncoderFactory,
                MessageHandlerFactory messageHandlerFactory,
                @Assisted Boolean isClientInitializer) {
        this.messageDecoderFactory = messageDecoderFactory;
        this.messageEncoderFactory = messageEncoderFactory;
        this.messageHandlerFactory = messageHandlerFactory;
        clientInitializer = isClientInitializer;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // add extra level of logging during testing
        if (clientInitializer) {
            pipeline.addLast(CLIENT_LOGGER_NAME, new LoggingHandler(CLIENT_LOGGER_NAME));
        } else {
            pipeline.addLast(SERVER_LOGGER_NAME, new LoggingHandler(SERVER_LOGGER_NAME));
        }
        pipeline.addLast(DECODER_NAME, messageDecoderFactory.create());
        pipeline.addLast(ENCODER_NAME, messageEncoderFactory.create());
        pipeline.addLast(HANDLER_NAME, messageHandlerFactory.create(clientInitializer));
    }
}
