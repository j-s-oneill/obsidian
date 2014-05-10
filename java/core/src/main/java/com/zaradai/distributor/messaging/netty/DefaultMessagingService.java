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

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.messaging.Connection;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingException;
import com.zaradai.distributor.messaging.MessagingService;

import java.net.InetSocketAddress;

public class DefaultMessagingService extends AbstractIdleService implements MessagingService {
    private final EventPublisher eventPublisher;
    private final ConnectionManager connectionManager;
    private final NettyServer server;
    private final EventLoopGroups eventLoopGroups;

    @Inject
    DefaultMessagingService(EventPublisher eventPublisher, ConnectionManager connectionManager, NettyServer server,
                            EventLoopGroups eventLoopGroups) {
        this.eventPublisher = eventPublisher;
        this.connectionManager = connectionManager;
        this.server = server;
        this.eventLoopGroups = eventLoopGroups;
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
        // finally shutdown the event loops
        eventLoopGroups.shutdown();
    }

    @Override
    public void publish(Message message) {
        for (Connection connection : connectionManager.getAll()) {
            sendMessage(connection, message);
        }

    }

    private void sendMessage(Connection connection, Message message)  {
        try {
            connection.send(message);
        } catch (MessagingException e) {
            // notify of send error
            eventPublisher.publish(new MessageErrorEvent(message, e.getMessage()));
        }
    }

    @Override
    public void send(InetSocketAddress target, Message message) {
        Connection connection = connectionManager.getForEndpoint(target);

        if (connection != null) {
            sendMessage(connection, message);
        } else {
            createConnectionAndSend(target, message);
        }
    }

    private void createConnectionAndSend(InetSocketAddress target, Message message) {
        // create a connection
        Connection connection = connectionManager.getOrCreate(target);
        // and send on it
        sendMessage(connection, message);
    }
}
