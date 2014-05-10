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
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.zaradai.config.ConfigurationSource;
import com.zaradai.config.InMemoryConfigurationSource;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.config.DistributorConfigImpl;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.messaging.Connection;
import com.zaradai.distributor.messaging.ConnectionFactory;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.MessagingService;
import com.zaradai.distributor.messaging.netty.ChannelConnection;
import com.zaradai.distributor.messaging.netty.DefaultMessagingService;
import com.zaradai.distributor.messaging.netty.EventLoopGroups;
import com.zaradai.distributor.messaging.netty.NettyClientFactory;
import com.zaradai.distributor.messaging.netty.NettyEventPublisher;
import com.zaradai.distributor.messaging.netty.handler.HandshakeHandlerFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageDecoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageEncoderFactory;
import com.zaradai.distributor.messaging.netty.handler.MessageHandlerFactory;
import com.zaradai.events.EventAggregator;
import com.zaradai.events.eventbus.EventBusAggregator;
import com.zaradai.net.authentication.AcceptIfOnApprovedListConnectionAuthenticator;
import com.zaradai.net.authentication.ConnectionAuthenticator;
import com.zaradai.serialization.Serializer;
import com.zaradai.serialization.kryo.KryoSerializer;

public class DistributorModule extends AbstractModule {
    private final EventBus eventBus;
    private final Kryo kryo;

    public DistributorModule() {
        eventBus = new EventBus("Distributor");
        kryo = new Kryo();
    }

    @Override
    protected void configure() {
        bindEventAggregator();
        bindConfig();
        bindSerialization();

        bind(MessagingService.class).to(DefaultMessagingService.class);
        bind(ConnectionManager.class).in(Singleton.class);
        bind(ConnectionAuthenticator.class).to(AcceptIfOnApprovedListConnectionAuthenticator.class).in(Singleton.class);
        bindNetty();
    }

    protected void bindNetty() {
        bind(EventLoopGroups.class).in(Singleton.class);
        bind(EventPublisher.class).to(NettyEventPublisher.class);

        install(new FactoryModuleBuilder()
                .implement(Connection.class, ChannelConnection.class).build(ConnectionFactory.class));
        install(new FactoryModuleBuilder().build(NettyClientFactory.class));
        install(new FactoryModuleBuilder().build(MessageDecoderFactory.class));
        install(new FactoryModuleBuilder().build(MessageEncoderFactory.class));
        install(new FactoryModuleBuilder().build(MessageHandlerFactory.class));
        install(new FactoryModuleBuilder().build(HandshakeHandlerFactory.class));
    }

    protected void bindSerialization() {
        bind(Kryo.class).toInstance(kryo);
        bind(Serializer.class).to(KryoSerializer.class).in(Singleton.class);
    }

    protected void bindEventAggregator() {
        bind(EventBus.class).toInstance(eventBus);
        bind(EventAggregator.class).to(EventBusAggregator.class);
    }

    protected void bindConfig() {
        bind(ConfigurationSource.class).to(InMemoryConfigurationSource.class).asEagerSingleton();
        bind(DistributorConfig.class).to(DistributorConfigImpl.class);
    }
}
