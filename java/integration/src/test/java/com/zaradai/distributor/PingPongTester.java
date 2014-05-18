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

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.events.MessageErrorEvent;
import com.zaradai.distributor.events.MessageSentEvent;
import com.zaradai.distributor.events.NodeConnectedEvent;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.events.EventAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PingPongTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingPongTester.class);

    private final EventAggregator eventAggregator;
    private final InetSocketAddress source;
    private CountDownLatch gate;

    @Inject
    PingPongTester(EventAggregator eventAggregator, DistributorConfig config) {
        this.eventAggregator = eventAggregator;
        source = createSource(config);

        eventAggregator.subscribe(this);
    }

    public void test(List<InetSocketAddress> targets) {
        if (gate == null) {
            gate = new CountDownLatch(targets.size());

            for (InetSocketAddress target : targets) {
                LOGGER.info("Pinging: {}", target);
                eventAggregator.publish(new Message.Builder().addTarget(target).event(new PingEvent(source)).build());
            }
        }
    }

    public boolean waitUntilFinishes(long duration, TimeUnit unit) {
        boolean res;

        try {
            res = gate.await(duration, unit);
        } catch (InterruptedException e) {
            res = false;
        }

        return res;
    }

    @Subscribe
    public void onPong(PongEvent event) {
        LOGGER.info("On Pong: {}", event.getFrom());
        if (gate != null) {
            gate.countDown();
        }
    }

    @Subscribe
    public void onPing(PingEvent event) {
        LOGGER.info("On Ping: {}", event.getFrom());

        eventAggregator.publish(new Message.Builder().addTarget(event.getFrom()).event(new PongEvent(source)).build());
    }

    @Subscribe
    public void onMessageErrorEvent(MessageErrorEvent event) {
        LOGGER.info("Error Sending, {}", event.getCause());
    }

    @Subscribe
    public void onMessageSentEvent(MessageSentEvent event) {
        LOGGER.info("Message Sent");
    }

    @Subscribe
    public void onNodeConnected(NodeConnectedEvent event) {
        LOGGER.info("Node Connected, {}", event.getAddress());
    }

    @Subscribe
    public void onNodeDisconnected(NodeConnectedEvent event) {
        LOGGER.info("Node Disconnected, {}", event.getAddress());
    }

    @Subscribe
    public void onDeadEvent(DeadEvent event) {
        LOGGER.info("No Handler for event, {}", event.getEvent());
    }

    private InetSocketAddress createSource(DistributorConfig config) {
        InetSocketAddress res;

        try {
            res = new InetSocketAddress(config.getHost(), config.getPort());
        } catch (Exception e) {
            res = new InetSocketAddress(config.getPort());
        }

        return res;
    }
}
