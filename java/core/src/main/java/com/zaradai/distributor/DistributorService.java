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
package com.zaradai.distributor;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingService;
import com.zaradai.events.EventAggregator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

public class DistributorService extends AbstractIdleService {
    private final EventAggregator eventAggregator;
    private final MessagingService messagingService;

    @Inject
    DistributorService(EventAggregator eventAggregator, MessagingService messagingService) {
        this.eventAggregator = eventAggregator;
        this.messagingService = messagingService;

        this.eventAggregator.subscribe(this);
    }

    /**
     * Subscribe for all message based events and distribute across grid.
     *
     * @param message
     */
    @Subscribe
    public void onMessage(Message message) {
        // distribute
        if (message.isIncoming()) {
            // if the message is incoming post the event
            incoming(message);
        } else {
            // put it onto the wire
            outgoing(message);
        }
    }


    @Override
    protected void startUp() throws Exception {
        messagingService.startAsync().awaitRunning();
    }

    @Override
    protected void shutDown() throws Exception {
        messagingService.stopAsync().awaitTerminated();
    }

    private void outgoing(Message message) {
        // get set of targets
        Set<InetSocketAddress> targets = message.getTargets();
        // clear the targets from the message to reduce size on the wire
        // as this information is not required anymore
        message.clearTargets();
        // if no targets then publish to all connected clients
        if (targets.isEmpty()) {
            messagingService.publish(message);
        } else {
            // iterate all targets and send one by obe
            for (InetSocketAddress target : targets) {
                messagingService.send(target, message);
            }
        }
    }

    private void incoming(Message message) {
        eventAggregator.publish(message.getEvent());
    }
}
