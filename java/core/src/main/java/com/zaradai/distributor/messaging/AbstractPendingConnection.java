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

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractPendingConnection implements Connection {
    private final BlockingQueue<Message> pendingQueue;

    protected AbstractPendingConnection() {
        pendingQueue = createPendingQueue();
    }

    protected BlockingQueue<Message> createPendingQueue() {
        return Queues.newLinkedBlockingQueue();
    }

    @Override
    public void send(Message message) throws MessagingException {
        if (isConnected()) {
            doSend(message);
        } else {
            // store all messages until the connection lives again
            try {
                pendingQueue.put(message);
            } catch (InterruptedException e) {
                throw new MessagingException("Interrupted whilst caching message to send", e);
            }
            // attempt to connect
            connect();
        }
    }

    protected abstract void connect();
    protected abstract void doSend(Message message) throws MessagingException;
    protected abstract boolean isConnected();


    protected List<Message> drainPending() {
        final List<Message> drained = Lists.newArrayList();
        // drain all pending messages
        pendingQueue.drainTo(drained);

        return drained;
    }
}
