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

import com.zaradai.mocks.ConnectionFactoryMocker;
import com.zaradai.mocks.ConnectionMocker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionManagerTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private static final Connection TEST_CONNECTOR = ConnectionMocker.create();
    private ConnectionFactory connectionFactory;
    @Mock
    ConcurrentMap<InetSocketAddress, Connection> mockMap;
    private ConnectionManager uut;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        connectionFactory = ConnectionFactoryMocker.create();
        uut = new ConnectionManager(connectionFactory) {
            @Override
            protected ConcurrentMap<InetSocketAddress, Connection> createConnectionsMap() {
                return mockMap;
            }
        };
    }

    @Test
    public void shouldAdd() {
        uut.add(TEST_ADDRESS, TEST_CONNECTOR);

        verify(mockMap).put(TEST_ADDRESS, TEST_CONNECTOR);
    }

    @Test
    public void shouldRemove() {
       uut.remove(TEST_ADDRESS);

        verify(mockMap).remove(TEST_ADDRESS);
    }

    @Test
    public void shouldGetKnownAddresses() throws Exception {
        uut = new ConnectionManager(connectionFactory);
        uut.add(TEST_ADDRESS, TEST_CONNECTOR);

        Set<InetSocketAddress> res = uut.getKnownAddresses();

        assertThat(res.size(), is(1));
    }

    @Test
    public void shouldGetForEndpoint() throws Exception {
        uut = new ConnectionManager(connectionFactory);
        uut.add(TEST_ADDRESS, TEST_CONNECTOR);

        Connection res = uut.getForEndpoint(TEST_ADDRESS);

        assertThat(res, is(TEST_CONNECTOR));
    }

    @Test
    public void shouldGetForAddress() throws Exception {
        InetAddress local = InetAddress.getLocalHost();
        uut = new ConnectionManager(connectionFactory);
        uut.add(new InetSocketAddress(local, 80), ConnectionMocker.create());
        uut.add(new InetSocketAddress(local, 81), ConnectionMocker.create());
        uut.add(new InetSocketAddress(local, 82), ConnectionMocker.create());

        Set<Connection> res = uut.getForAddress(local);

        assertThat(res.size(), is(3));
    }

    @Test
    public void shouldGetAll() throws Exception {
        InetAddress local = InetAddress.getLocalHost();
        uut = new ConnectionManager(connectionFactory);
        uut.add(new InetSocketAddress(local, 80), ConnectionMocker.create());
        uut.add(new InetSocketAddress(local, 81), ConnectionMocker.create());
        uut.add(new InetSocketAddress(local, 82), ConnectionMocker.create());

        Set<Connection> res = uut.getForAddress(local);

        assertThat(res.size(), is(3));
    }

    @Test
    public void shouldShutdown() throws Exception {
        uut = new ConnectionManager(connectionFactory);
        uut.add(TEST_ADDRESS, TEST_CONNECTOR);

        uut.shutdown();

        verify(TEST_CONNECTOR).shutdown();
    }

    @Test
    public void shouldCreateAndAddIfNotExistsAlready() throws Exception {
        when(connectionFactory.create(TEST_ADDRESS)).thenReturn(TEST_CONNECTOR);
        uut = new ConnectionManager(connectionFactory);

        Connection res = uut.getOrCreate(TEST_ADDRESS);

        assertThat(res, not(nullValue()));
        assertThat(res, is(TEST_CONNECTOR));
    }

    @Test
    public void shouldReturnExistingIfGetOrCreateWithExistingEndpoint() throws Exception {
        Connection existing = ConnectionMocker.create();
        when(connectionFactory.create(TEST_ADDRESS)).thenReturn(TEST_CONNECTOR);
        uut = new ConnectionManager(connectionFactory);
        uut.add(TEST_ADDRESS, existing);

        Connection res = uut.getOrCreate(TEST_ADDRESS);

        assertThat(res, not(nullValue()));
        assertThat(res, is(existing));
    }
}
