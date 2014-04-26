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
package com.zaradai.distributor.messaging;

import com.zaradai.mocks.ClientFactoryMocker;
import com.zaradai.mocks.ClientMocker;
import com.zaradai.mocks.ConnectionFactoryMocker;
import com.zaradai.mocks.ConnectionMocker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ConnectionManagerTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private static final Connection TEST_CONNECTION = ConnectionMocker.create();
    private static final Client TEST_CLIENT = ClientMocker.create();

    private ConnectionFactory connectionFactory;
    private ClientFactory clientFactory;
    private ConnectionManager uut;

    @Mock
    ConcurrentMap<InetSocketAddress, Connection> mockMap;

    @Before
    public void setUp() throws Exception {
        connectionFactory = ConnectionFactoryMocker.create(TEST_CONNECTION);
        clientFactory = ClientFactoryMocker.create(TEST_CLIENT, TEST_ADDRESS);
        uut = new ConnectionManager(connectionFactory, clientFactory);
    }

    @Test
    public void shouldCreateConnectionIfAbsent() throws Exception {
        Connection res = uut.get(TEST_ADDRESS, false);

        assertThat(res, is(TEST_CONNECTION));
    }

    @Test
    public void shouldCreateAndConnectIfAbsentAndConnectRequested() throws Exception {
        Connection res = uut.get(TEST_ADDRESS, true);

        assertThat(res, is(TEST_CONNECTION));
        verify(clientFactory).create(TEST_ADDRESS);
        verify(TEST_CLIENT).connect(res);
    }

    @Test
    public void shouldCreateAndNotConnectIfAbsentAndConnectNotRequested() throws Exception {
        Connection res = uut.get(TEST_ADDRESS, false);

        assertThat(res, is(TEST_CONNECTION));
        verify(connectionFactory).create();
        verify(clientFactory, never()).create(TEST_ADDRESS);
    }

    @Test
    public void shouldEnsureConnectionAtomicity() throws Exception {
        MockitoAnnotations.initMocks(this);
        final Connection EXISTING = ConnectionMocker.create();
        uut = new ConnectionManager(connectionFactory, clientFactory) {
            @Override
            protected ConcurrentMap<InetSocketAddress, Connection> createConnectionsMap() {
                return mockMap;
            }
        };
        // when putIfAbsent called return an existing connection
        when(mockMap.putIfAbsent(TEST_ADDRESS, TEST_CONNECTION)).thenReturn(EXISTING);

        Connection res = uut.get(TEST_ADDRESS, true);

        verify(clientFactory, never()).create(TEST_ADDRESS);
        assertThat(res, is(EXISTING));
    }

    @Test
    public void shouldReturnCreatedConnections() throws Exception {
        // create the connection
        uut.get(TEST_ADDRESS, false);

        Set<InetSocketAddress> addresses = uut.getKnownAddresses();

        assertThat(addresses.isEmpty(), is(false));
        assertThat(addresses.contains(TEST_ADDRESS), is(true));
    }

    @Test
    public void shouldShutdownAllKnownConnections() throws Exception {
        uut.get(TEST_ADDRESS, false);

        uut.shutdown();

        verify(TEST_CONNECTION).shutdown();
    }
}
