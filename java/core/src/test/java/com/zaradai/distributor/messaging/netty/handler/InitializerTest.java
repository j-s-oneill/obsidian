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

import com.zaradai.mocks.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class InitializerTest {
    private MessageDecoderFactory messageDecoderFactory;
    private MessageDecoder messageDecoder;
    private MessageEncoderFactory messageEncoderFactory;
    private MessageEncoder messageEncoder;
    private SocketChannel channel;
    private ChannelPipeline pipeline;

    @Before
    public void setUp() throws Exception {
        messageDecoder = MessageDecoderMocker.create();
        messageDecoderFactory = MessageDecoderFactoryMocker.create(messageDecoder);
        messageEncoder = MessageEncoderMocker.create();
        messageEncoderFactory = MessageEncoderFactoryMocker.create(messageEncoder);
        channel = mock(SocketChannel.class);
        pipeline = mock(ChannelPipeline.class);
        when(channel.pipeline()).thenReturn(pipeline);
    }

    @Test
    public void shouldInitializeClientHandler() throws Exception {
        boolean isClientHandler = true;
        MessageHandler handler = MessageHandlerMocker.create();
        MessageHandlerFactory factory = MessageHandlerFactoryMocker.create(isClientHandler, handler);
        Initializer uut = new Initializer(messageDecoderFactory, messageEncoderFactory, factory,isClientHandler);

        uut.initChannel(channel);

        ArgumentCaptor<ChannelHandler> handlerCaptor = ArgumentCaptor.forClass(ChannelHandler.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(pipeline, times(4)).addLast(nameCaptor.capture(), handlerCaptor.capture());
        // assert type and order
        assertThat(nameCaptor.getAllValues().get(0), is(Initializer.CLIENT_LOGGER_NAME));

        assertThat(nameCaptor.getAllValues().get(1), is(Initializer.DECODER_NAME));
        assertThat((MessageDecoder) handlerCaptor.getAllValues().get(1), is(messageDecoder));

        assertThat(nameCaptor.getAllValues().get(2), is(Initializer.ENCODER_NAME));
        assertThat((MessageEncoder) handlerCaptor.getAllValues().get(2), is(messageEncoder));

        assertThat(nameCaptor.getAllValues().get(3), is(Initializer.HANDLER_NAME));
        assertThat((MessageHandler) handlerCaptor.getAllValues().get(3), is(handler));
    }

    @Test
    public void shouldInitializeServerHandler() throws Exception {
        boolean isClientHandler = false;
        MessageHandler handler = MessageHandlerMocker.create();
        MessageHandlerFactory factory = MessageHandlerFactoryMocker.create(isClientHandler, handler);
        Initializer uut = new Initializer(messageDecoderFactory, messageEncoderFactory, factory,isClientHandler);

        uut.initChannel(channel);

        ArgumentCaptor<ChannelHandler> handlerCaptor = ArgumentCaptor.forClass(ChannelHandler.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(pipeline, times(4)).addLast(nameCaptor.capture(), handlerCaptor.capture());
        // assert type and order
        assertThat(nameCaptor.getAllValues().get(0), is(Initializer.SERVER_LOGGER_NAME));

        assertThat(nameCaptor.getAllValues().get(1), is(Initializer.DECODER_NAME));
        assertThat((MessageDecoder) handlerCaptor.getAllValues().get(1), is(messageDecoder));

        assertThat(nameCaptor.getAllValues().get(2), is(Initializer.ENCODER_NAME));
        assertThat((MessageEncoder) handlerCaptor.getAllValues().get(2), is(messageEncoder));

        assertThat(nameCaptor.getAllValues().get(3), is(Initializer.HANDLER_NAME));
        assertThat((MessageHandler) handlerCaptor.getAllValues().get(3), is(handler));
    }
}
