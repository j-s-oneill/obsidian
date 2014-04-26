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

public abstract class AbstractPendingCacheConnection implements Connection {
    private final BlockingQueue<Message> pending;

    protected AbstractPendingCacheConnection() {
        pending = createPendingQueue();
    }

    protected BlockingQueue<Message> createPendingQueue() {
        return Queues.newLinkedBlockingQueue();
    }

    @Override
    public void send(Message message) throws MessagingException {
        if (!isConnected()) {
            // cache all messages until the connection lives again
            try {
                pending.put(message);
            } catch (InterruptedException e) {
                throw new MessagingException("Interrupted whilst caching message to send", e);
            }
        } else {
            doSend(message);
        }
    }

    protected abstract void doSend(Message message) throws MessagingException;
    protected abstract boolean isConnected();

    /**
     * Called when an active connection is made.  Any pending messages collected
     * whilst the connection was down will attempt to send again.
     * @throws MessagingException
     */
    protected void flushPendingMessages() throws MessagingException {
        final List<Message> drained = Lists.newArrayList();
        pending.drainTo(drained);
        // try and resend
        for (Message message : drained) {
            send(message);
        }
    }
}
