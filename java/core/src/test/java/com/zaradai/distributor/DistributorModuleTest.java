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
package com.zaradai.distributor;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaradai.config.ConfigurationSource;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.messaging.ConnectionFactory;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.MessagingService;
import com.zaradai.distributor.messaging.netty.EventLoopGroups;
import com.zaradai.distributor.messaging.netty.NettyClientFactory;
import com.zaradai.distributor.messaging.netty.handler.HandshakeHandlerFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageDecoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageEncoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageHandlerFactory;
import com.zaradai.events.EventAggregator;
import com.zaradai.net.authentication.ConnectionAuthenticator;
import com.zaradai.serialization.Serializer;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DistributorModuleTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new DistributorModule());
    }

    @Test
    public void shouldConfigure() throws Exception {
        assertThat(injector.getInstance(EventBus.class), not(nullValue()));
        assertThat(injector.getInstance(EventAggregator.class), not(nullValue()));
        assertThat(injector.getInstance(ConfigurationSource.class), not(nullValue()));
        assertThat(injector.getInstance(DistributorConfig.class), not(nullValue()));
        assertThat(injector.getInstance(Kryo.class), not(nullValue()));
        assertThat(injector.getInstance(Serializer.class), not(nullValue()));
        assertThat(injector.getInstance(MessagingService.class), not(nullValue()));
        assertThat(injector.getInstance(ConnectionManager.class), not(nullValue()));
        assertThat(injector.getInstance(ConnectionAuthenticator.class), not(nullValue()));
        assertThat(injector.getInstance(EventLoopGroups.class), not(nullValue()));
        assertThat(injector.getInstance(EventPublisher.class), not(nullValue()));
        assertThat(injector.getInstance(ConnectionFactory.class), not(nullValue()));
        assertThat(injector.getInstance(NettyClientFactory.class), not(nullValue()));
        assertThat(injector.getInstance(MessageDecoderFactory.class), not(nullValue()));
        assertThat(injector.getInstance(MessageEncoderFactory.class), not(nullValue()));
        assertThat(injector.getInstance(MessageHandlerFactory.class), not(nullValue()));
        assertThat(injector.getInstance(HandshakeHandlerFactory.class), not(nullValue()));
    }

    @Test
    public void shouldCreateInCorrectScope() throws Exception {
        // Singletons
        assertThat(injector.getInstance(EventBus.class), is(injector.getInstance(EventBus.class)));
        assertThat(injector.getInstance(ConfigurationSource.class), is(injector.getInstance(ConfigurationSource.class)));
        assertThat(injector.getInstance(ConnectionManager.class), is(injector.getInstance(ConnectionManager.class)));
        assertThat(injector.getInstance(ConnectionAuthenticator.class), is(injector.getInstance(ConnectionAuthenticator.class)));
        assertThat(injector.getInstance(EventLoopGroups.class), is(injector.getInstance(EventLoopGroups.class)));
        assertThat(injector.getInstance(ConnectionFactory.class), is(injector.getInstance(ConnectionFactory.class)));
        assertThat(injector.getInstance(NettyClientFactory.class), is(injector.getInstance(NettyClientFactory.class)));
        assertThat(injector.getInstance(MessageDecoderFactory.class), is(injector.getInstance(MessageDecoderFactory.class)));
        assertThat(injector.getInstance(MessageEncoderFactory.class), is(injector.getInstance(MessageEncoderFactory.class)));
        assertThat(injector.getInstance(MessageHandlerFactory.class), is(injector.getInstance(MessageHandlerFactory.class)));
        assertThat(injector.getInstance(HandshakeHandlerFactory.class), is(injector.getInstance(HandshakeHandlerFactory.class)));
        assertThat(injector.getInstance(Serializer.class), is(injector.getInstance(Serializer.class)));
        // Scoped instances
        assertThat(injector.getInstance(EventAggregator.class), not(injector.getInstance(EventAggregator.class)));
        assertThat(injector.getInstance(DistributorConfig.class), not(injector.getInstance(DistributorConfig.class)));
        assertThat(injector.getInstance(MessagingService.class), not(injector.getInstance(MessagingService.class)));
        assertThat(injector.getInstance(EventPublisher.class), not(injector.getInstance(EventPublisher.class)));
    }

    @Test
    public void shouldBuildFactoryInstances() throws Exception {
        ConnectionFactory factory = injector.getInstance(ConnectionFactory.class);
        assertThat(factory.create(TEST_ADDRESS), not(nullValue()));
    }

    @Test
    public void shouldBuildClient() throws Exception {
        NettyClientFactory factory = injector.getInstance(NettyClientFactory.class);
        assertThat(factory.create(TEST_ADDRESS), not(nullValue()));
    }

    @Test
    public void shouldBuildDecoder() throws Exception {
        MessageDecoderFactory factory = injector.getInstance(MessageDecoderFactory.class);
        assertThat(factory.create(), not(nullValue()));
    }

    @Test
    public void shouldBuildEncoder() throws Exception {
        MessageEncoderFactory factory = injector.getInstance(MessageEncoderFactory.class);
        assertThat(factory.create(), not(nullValue()));
    }

    @Test
    public void shouldBuildMessageHandler() throws Exception {
        MessageHandlerFactory factory = injector.getInstance(MessageHandlerFactory.class);
        assertThat(factory.create(), not(nullValue()));
    }

    @Test
    public void shouldBuildHandshakeHandler() throws Exception {
        HandshakeHandlerFactory factory = injector.getInstance(HandshakeHandlerFactory.class);
        assertThat(factory.create(true), not(nullValue()));
    }

}

