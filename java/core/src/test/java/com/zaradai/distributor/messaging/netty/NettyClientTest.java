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
import com.zaradai.distributor.messaging.netty.handler.Initializer;
import com.zaradai.distributor.messaging.netty.handler.InitializerFactory;
import com.zaradai.mocks.DistributorConfigMocker;
import com.zaradai.mocks.InitializerFactoryMocker;
import com.zaradai.mocks.InitializerMocker;
import io.netty.channel.EventLoopGroup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NettyClientTest {
    private DistributorConfig config;
    private InitializerFactory initializerFactory;
    @Mock
    private EventLoopGroups eventLoopGroups;
    @Mock
    private InetSocketAddress endpoint;
    @Mock
    private EventLoopGroup eventLoopGroup;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        config = DistributorConfigMocker.create();
        when(config.getTcpNoDelay()).thenReturn(true);
        when(config.getKeepAlive()).thenReturn(true);
        when(config.getConnectionTimeout()).thenReturn(5000);

        when(eventLoopGroups.getClientGroup()).thenReturn(eventLoopGroup);
        Initializer initializer = InitializerMocker.create();
        initializerFactory = InitializerFactoryMocker.create(true, initializer);
    }

    @Test
    public void shouldCreateUsingConfig() throws Exception {
        NettyClient uut = new NettyClient(config, eventLoopGroups, initializerFactory, endpoint);

        verify(config).getTcpNoDelay();
        verify(config).getKeepAlive();
        verify(config).getConnectionTimeout();
    }
}
