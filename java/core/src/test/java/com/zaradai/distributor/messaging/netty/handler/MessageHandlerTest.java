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

import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.events.NodeConnectedEvent;
import com.zaradai.distributor.events.NodeDisconnectedEvent;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.netty.ChannelConnection;
import com.zaradai.events.EventAggregator;
import com.zaradai.mocks.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MessageHandlerTest {
    private static final int TEST_PORT = 345;
    private static final int LISTEN_PORT = 346;
    private static final Message TEST_MESSAGE = MessageMocker.create();
    private EventAggregator eventAggregator;
    private DistributorConfig config;
    private ConnectionManager connectionManager;
    private ChannelHandlerContext ctx;
    private Channel channel;
    private ChannelConnection connection;
    private InetSocketAddress address;

    @Before
    public void setUp() throws Exception {
        eventAggregator = EventAggregatorMocker.create();
        config = DistributorConfigMocker.create();
        when(config.getPort()).thenReturn(LISTEN_PORT);
        connectionManager = ConnectionManagerMocker.create();
        connection = ChannelConnectionMocker.create();
        ctx = mock(ChannelHandlerContext.class);
        channel = mock(Channel.class);
        when(ctx.channel()).thenReturn(channel);
        address = new InetSocketAddress("127.0.0.1", TEST_PORT);
        when(channel.remoteAddress()).thenReturn(address);
        when(connectionManager.getOrCreate(address)).thenReturn(connection);
    }

}
