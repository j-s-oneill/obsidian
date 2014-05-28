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

import ch.qos.logback.core.Appender;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.events.MessageSentEvent;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.mocks.EventPublisherMocker;
import com.zaradai.mocks.MessageMocker;
import com.zaradai.mocks.NettyClientFactoryMocker;
import com.zaradai.mocks.NettyClientMocker;
import com.zaradai.util.LoggerTester;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.net.InetSocketAddress;
import java.util.List;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ChannelConnectionTest {
    private final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private EventPublisher eventPublisher;
    private NettyClientFactory nettyClientFactory;
    private ChannelConnection uut;
    private Channel channel;
    private ChannelFuture future;
    private NettyClient client;

    @Before
    public void setUp() throws Exception {
        eventPublisher = EventPublisherMocker.create();
        nettyClientFactory = NettyClientFactoryMocker.create();
        channel = mock(Channel.class);
        future = mock(ChannelFuture.class);
        when(channel.closeFuture()).thenReturn(future);
        client = NettyClientMocker.create();
        when(nettyClientFactory.create(TEST_ADDRESS)).thenReturn(client);

        uut = new ChannelConnection(eventPublisher, nettyClientFactory, TEST_ADDRESS);
    }

    @Test
    public void shouldLogOnSetChannel() throws Exception {
        Appender appender = LoggerTester.create();

        uut.setChannel(channel);

        List<String> logged = LoggerTester.captureLogMessages(appender);
        assertThat(logged.get(0), containsString("Activating"));
    }

    @Test
    public void shouldAddCloseListenerOnSetChannel() throws Exception {
        uut.setChannel(channel);

        verify(future).addListener((GenericFutureListener<? extends Future<? super Void>>) Matchers.any());
    }

    @Test
    public void shouldBeConnectedOnSetChannel() throws Exception {
        uut.setChannel(channel);

        assertThat(uut.isConnected(), is(true));
    }

    @Test
    public void shouldLogOnChannelRemoved() throws Exception {
        Appender appender = LoggerTester.create();

        uut.setChannel(null);

        List<String> logged = LoggerTester.captureLogMessages(appender);
        assertThat(logged.get(0), containsString("Deactivating"));
    }

    @Test
    public void shouldRemoveHandlerOnChannelRemoved() throws Exception {
        uut.setChannel(channel);

        uut.setChannel(null);

        verify(future).removeListener((GenericFutureListener<? extends Future<? super Void>>) Matchers.any());
    }

    @Test
    public void shouldCloseChannelOnShutdown() throws Exception {
        uut.setChannel(channel);

        uut.shutdown();

        verify(channel).close();
    }

    @Test
    public void shouldAttemptConnectOnFirstConnect() throws Exception {
        uut.connect();

        verify(nettyClientFactory).create(TEST_ADDRESS);
        verify(client).connect();
    }

    @Test
    public void shouldNotReconnectOnSubsequentConnects() throws Exception {
        uut.connect();

        uut.connect();

        verify(nettyClientFactory).create(TEST_ADDRESS);
        verify(client).connect();
    }

    @Test
    public void shouldSendMessageAfterChannelConnected() throws Exception {
        Message testMessage = MessageMocker.create();
        ChannelFuture channelFuture = mock(ChannelFuture.class);
        when(channel.writeAndFlush(testMessage)).thenReturn(channelFuture);
        uut.send(testMessage);

        uut.setChannel(channel);

        verify(channel).writeAndFlush(testMessage);
    }

    @Test
    public void shouldPublishOnSuccess() throws Exception {
        uut.onSuccess(MessageMocker.create());

        verify(eventPublisher).publish(Matchers.any(MessageSentEvent.class));
    }

    @Test
    public void shouldPublishOnFailure() throws Exception {
        uut.onFailure(MessageMocker.create(), new Exception());

        verify(eventPublisher).publish(Matchers.any(MessageErrorEvent.class));
    }
}
