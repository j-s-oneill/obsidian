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

import com.google.common.collect.Sets;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.messaging.Connection;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingException;
import com.zaradai.mocks.*;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

public class DefaultMessagingServiceTest {
    private static final Message TEST_MESSAGE = MessageMocker.create();
    private final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private EventPublisher eventPublisher;
    private ConnectionManager connectionManager;
    private NettyServer nettyServer;
    private EventLoopGroups eventLoopGroups;
    private DefaultMessagingService uut;

    @Before
    public void setUp() throws Exception {
        eventPublisher = EventPublisherMocker.create();
        connectionManager = ConnectionManagerMocker.create();
        nettyServer = NettyServerMocker.create();
        eventLoopGroups = EventLoopGroupsMocker.create();
        uut = new DefaultMessagingService(eventPublisher, connectionManager, nettyServer, eventLoopGroups);
    }

    @Test
    public void shouldStartUp() throws Exception {
        uut.startUp();

        verify(nettyServer).listen();
    }

    @Test
    public void shouldShutDown() throws Exception {
        uut.shutDown();

        verify(nettyServer).shutdown();
        verify(connectionManager).shutdown();
        verify(eventLoopGroups).shutdown();
    }

    @Test
    public void shouldPublish() throws Exception {
        Connection connection = ConnectionMocker.create();
        when(connectionManager.getAll()).thenReturn(Sets.newHashSet(connection));

        uut.publish(TEST_MESSAGE);

        verify(connection).send(TEST_MESSAGE);
    }

    @Test
    public void shouldPublishErrorIfSendThrows() throws Exception {
        Connection connection = ConnectionMocker.create();
        when(connectionManager.getAll()).thenReturn(Sets.newHashSet(connection));
        doThrow(MessagingException.class).when(connection).send(TEST_MESSAGE);

        uut.publish(TEST_MESSAGE);

        verify(eventPublisher).publish(any(MessageErrorEvent.class));
    }

    @Test
    public void shouldSend() throws Exception {
        Connection connection = ConnectionMocker.create();
        when(connectionManager.getForEndpoint(TEST_ADDRESS)).thenReturn(connection);

        uut.send(TEST_ADDRESS, TEST_MESSAGE);

        verify(connection).send(TEST_MESSAGE);
    }

    @Test
    public void shouldCreateConnectionIfNotExistsWhenSending() throws Exception {
        Connection connection = ConnectionMocker.create();
        when(connectionManager.getForEndpoint(TEST_ADDRESS)).thenReturn(null);
        when(connectionManager.getOrCreate(TEST_ADDRESS)).thenReturn(connection);

        uut.send(TEST_ADDRESS, TEST_MESSAGE);

        verify(connection).send(TEST_MESSAGE);

    }
}
