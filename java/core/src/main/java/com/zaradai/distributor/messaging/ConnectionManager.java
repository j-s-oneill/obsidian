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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManager {
    private final ConcurrentMap<InetSocketAddress, Connection> activeConnections;
    private final ConnectionFactory connectionFactory;

    @Inject
    ConnectionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        activeConnections = createConnectionsMap();
    }

    protected ConcurrentMap<InetSocketAddress, Connection> createConnectionsMap() {
        return Maps.newConcurrentMap();
    }

    public void add(InetSocketAddress endpoint, Connection connection) {
        activeConnections.put(endpoint, connection);
    }

    public void remove(InetSocketAddress endpoint) {
        activeConnections.remove(endpoint);
    }

    public List<InetSocketAddress> getKnownAddresses() {
        return ImmutableList.copyOf(activeConnections.keySet());
    }

    public Connection getForEndpoint(InetSocketAddress endpoint) {
        return activeConnections.get(endpoint);
    }

    public List<Connection> getForAddress(InetAddress address) {
        List<Connection> res = Lists.newArrayList();

        for (Map.Entry<InetSocketAddress, Connection> entry : activeConnections.entrySet()) {
            if (entry.getKey().getAddress().equals(address)) {
                res.add(entry.getValue());
            }
        }

        return res;
    }

    public List<Connection> getAll() {
        return ImmutableList.copyOf(activeConnections.values());
    }

    public Connection getOrCreate(InetSocketAddress endpoint) {
        Connection connection = connectionFactory.create(endpoint);
        Connection previous = activeConnections.putIfAbsent(endpoint, connection);

        if (previous != null) {
            return previous;
        }

        return connection;
    }

    public void shutdown() {
        for (Connection connection : getAll()) {
            connection.shutdown();
        }
    }
}
