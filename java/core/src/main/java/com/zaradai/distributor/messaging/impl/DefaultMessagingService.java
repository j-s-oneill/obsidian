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

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.messaging.Connection;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingException;
import com.zaradai.distributor.messaging.MessagingService;
import com.zaradai.distributor.messaging.Server;
import com.zaradai.events.EventAggregator;

import java.net.InetSocketAddress;

public class DefaultMessagingService extends AbstractIdleService implements MessagingService {
    private final EventAggregator eventAggregator;
    private final ConnectionManager connectionManager;
    private final Server server;

    @Inject
    DefaultMessagingService(EventAggregator eventAggregator, ConnectionManager connectionManager, Server server) {
        this.eventAggregator = eventAggregator;
        this.connectionManager = connectionManager;
        this.server = server;
    }

    @Override
    protected void startUp() throws Exception {
        server.listen();
    }

    @Override
    protected void shutDown() throws Exception {
        // stop listening and close down the server.
        server.shutdown();
        // Shutdown any active connections.
        connectionManager.shutdown();
    }

    @Override
    public void publish(Message message) {
        for (InetSocketAddress address : connectionManager.getKnownAddresses()) {
            send(address, message);
        }
    }

    @Override
    public void send(InetSocketAddress target, Message message) {
        Connection connection = connectionManager.get(target, true);

        try {
            connection.send(message);
        } catch (MessagingException e) {
            // notify of send error
            eventAggregator.publish(new MessageErrorEvent(message, e.getMessage()));
        }
    }
}
