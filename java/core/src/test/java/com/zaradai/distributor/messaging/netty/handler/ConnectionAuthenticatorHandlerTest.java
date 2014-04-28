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

import ch.qos.logback.core.Appender;
import com.zaradai.mocks.ConnectionAuthenticatorMocker;
import com.zaradai.net.authentication.ConnectionAuthenticator;
import com.zaradai.util.LoggerTester;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionAuthenticatorHandlerTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private ConnectionAuthenticator authenticator;
    private ConnectionAuthenticatorHandler uut;
    private ChannelHandlerContext ctx;
    private Channel channel;
    private ChannelPipeline pipeline;

    @Before
    public void setUp() throws Exception {
        ctx = mock(ChannelHandlerContext.class);
        channel = mock(Channel.class);
        pipeline = mock(ChannelPipeline.class);
        when(ctx.channel()).thenReturn(channel);
        when(ctx.pipeline()).thenReturn(pipeline);

        authenticator = ConnectionAuthenticatorMocker.create();
        uut = new ConnectionAuthenticatorHandler(authenticator);
    }

    @Test
    public void shouldAcceptAuthenticatedIncomingConnection() throws Exception {
        when(channel.remoteAddress()).thenReturn(TEST_ADDRESS);
        when(authenticator.authenticate(TEST_ADDRESS)).thenReturn(true);

        Appender appender = LoggerTester.create();
        uut.channelActive(ctx);

        List<String> res = LoggerTester.captureLogMessages(appender);

        assertThat(res.size(), is(1));
        assertThat(res.get(0), containsString("accepted"));
    }

    @Test
    public void shouldRejectUnAuthenticatedIncomingConnection() throws Exception {
        when(channel.remoteAddress()).thenReturn(TEST_ADDRESS);
        when(authenticator.authenticate(TEST_ADDRESS)).thenReturn(false);

        Appender appender = LoggerTester.create();
        uut.channelActive(ctx);

        List<String> res = LoggerTester.captureLogMessages(appender);

        assertThat(res.size(), is(1));
        assertThat(res.get(0), containsString("rejected"));
    }

    @Test
    public void shouldRemoveFromPipelineAfterActiveCalled() throws Exception {
        uut.channelActive(ctx);

        verify(pipeline).remove(uut);
    }

    @Test
    public void shouldLogInvalidRemoteAddress() throws Exception {
        when(channel.remoteAddress()).thenReturn(null);
        Appender appender = LoggerTester.create();

        uut.channelActive(ctx);

        List<String> res = LoggerTester.captureLogMessages(appender);

        assertThat(res.get(0), containsString("Remote address is invalid"));
    }
}
