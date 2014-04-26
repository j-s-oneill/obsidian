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
package com.zaradai.distributor.messaging.impl;

import com.google.common.collect.Sets;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.messaging.*;
import com.zaradai.events.EventAggregator;
import com.zaradai.mocks.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class DefaultMessagingServiceTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private static final Message TEST_MESSAGE = MessageMocker.create();
    private EventAggregator eventAggregator;
    private ConnectionManager connectionManager;
    private Server server;
    private DefaultMessagingService uut;

    @Before
    public void setUp() throws Exception {
        eventAggregator = EventAggregatorMocker.create();
        connectionManager = ConnectionManagerMocker.create();
        server = ServerMocker.create();
        uut = new DefaultMessagingService(eventAggregator, connectionManager, server);
    }

    @Test
    public void shouldStartUp() throws Exception {
        uut.startAsync().awaitRunning();

        verify(server).listen();
    }

    @Test
    public void shouldShutDown() throws Exception {
        uut.startAsync().awaitRunning();
        uut.stopAsync().awaitTerminated();

        verify(server).shutdown();
        verify(connectionManager).shutdown();
    }

    @Test
    public void shouldPublishToAllKnownTargets() throws Exception {
        Set<InetSocketAddress> knownAddress = Sets.newHashSet(TEST_ADDRESS);
        when(connectionManager.getKnownAddresses()).thenReturn(knownAddress);
        Connection connection = ConnectionMocker.create();
        when(connectionManager.get(TEST_ADDRESS, true)).thenReturn(connection);

        uut.publish(TEST_MESSAGE);

        verify(connection).send(TEST_MESSAGE);
    }

    @Test
    public void shouldPostErrorMessageIfCantSendMessage() throws Exception {
        Connection connection = ConnectionMocker.create();
        when(connectionManager.get(TEST_ADDRESS, true)).thenReturn(connection);
        Mockito.doThrow(MessagingException.class).when(connection).send(TEST_MESSAGE);
        uut.send(TEST_ADDRESS, TEST_MESSAGE);

        ArgumentCaptor<MessageErrorEvent> captor = ArgumentCaptor.forClass(MessageErrorEvent.class);
        verify(eventAggregator).publish(captor.capture());

        assertThat(captor.getValue().getMessage(), is(TEST_MESSAGE));
    }
}
