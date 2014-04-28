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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManager {
    private final ConcurrentMap<InetSocketAddress, Connection> connections;
    private final ConnectionFactory connectionFactory;
    private final ClientFactory clientFactory;

    @Inject
    ConnectionManager(ConnectionFactory connectionFactory, ClientFactory clientFactory) {
        this.connectionFactory = connectionFactory;
        this.clientFactory = clientFactory;
        connections = createConnectionsMap();
    }

    protected ConcurrentMap<InetSocketAddress, Connection> createConnectionsMap() {
        return Maps.newConcurrentMap();
    }

    public Set<InetSocketAddress> getKnownAddresses() {
        return ImmutableSet.copyOf(connections.keySet());
    }

    public Connection get(InetSocketAddress target) {
        return get(target, false);
    }

    public Connection get(InetSocketAddress target, boolean tryConnectIfAbsent) {
        Connection connection = connections.get(target);

        if (connection == null) {
            connection = connectionFactory.create();
            // add atomically
            Connection existing = connections.putIfAbsent(target, connection);

            if (existing == null) {
                if (tryConnectIfAbsent) {
                    // ask a client to setup a connection to the target
                    clientFactory.create(target).connect(connection);
                }
            } else {
                connection = existing;
            }
        }

        return connection;
    }

    public void shutdown() {
        for (Map.Entry<InetSocketAddress, Connection> entry : connections.entrySet()) {
            entry.getValue().shutdown();
        }
    }
}
