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

import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.events.MessageSentEvent;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.mocks.EventPublisherMocker;
import com.zaradai.mocks.MessageMocker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NettyConnectionTest {
    private static final Message TEST_MESSAGE = MessageMocker.create();
    private static final Throwable TEST_CAUSE = new Exception();
    private EventPublisher eventPublisher;
    private NettyConnection uut;
    @Mock
    private Channel channel;
    @Mock
    private ChannelFuture channelFuture;
    @Mock
    private ChannelFuture closeFuture;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(channel.closeFuture()).thenReturn(closeFuture);
        when(channel.writeAndFlush(TEST_MESSAGE)).thenReturn(channelFuture);

        eventPublisher = EventPublisherMocker.create();
        uut = new NettyConnection(eventPublisher);
    }

    @Test
    public void shouldPublishOnSuccessfulSend() throws Exception {
        when(channelFuture.isSuccess()).thenReturn(true);
        uut.setChannel(channel);
        ArgumentCaptor<ChannelFutureListener> captor = ArgumentCaptor.forClass(ChannelFutureListener.class);

        uut.doSend(TEST_MESSAGE);
        verify(channel).writeAndFlush(TEST_MESSAGE);
        verify(channelFuture).addListener(captor.capture());
        // use the captured listener to trigger a publish
        captor.getValue().operationComplete(channelFuture);

        verify(eventPublisher).publish(any(MessageSentEvent.class));
    }

    @Test
    public void shouldPublishOnSendFailure() throws Exception {
        when(channelFuture.isSuccess()).thenReturn(false);
        when(channelFuture.cause()).thenReturn(TEST_CAUSE);
        uut.setChannel(channel);
        ArgumentCaptor<ChannelFutureListener> captor = ArgumentCaptor.forClass(ChannelFutureListener.class);

        uut.doSend(TEST_MESSAGE);
        verify(channelFuture).addListener(captor.capture());
        // use the captured listener to trigger a publish
        captor.getValue().operationComplete(channelFuture);

        verify(eventPublisher).publish(any(MessageErrorEvent.class));
    }

    @Test
    public void shouldNotBeConnectedIfChannelNotSet() throws Exception {
        assertThat(uut.isConnected(), is(false));
    }

    @Test
    public void shouldNotBeConnectedIfChannelSet() throws Exception {
        uut.setChannel(channel);

        assertThat(uut.isConnected(), is(true));
    }

    @Test
    public void shouldCloseChannelIfConnectedOnShutdown() throws Exception {
        uut.setChannel(channel);

        uut.shutdown();

        verify(channel).close();
    }
}
