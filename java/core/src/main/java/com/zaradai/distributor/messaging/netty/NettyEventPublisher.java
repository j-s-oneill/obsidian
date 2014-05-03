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

import com.google.inject.Inject;
import com.zaradai.distributor.events.EventPublisher;
import com.zaradai.events.EventAggregator;

import java.util.concurrent.ExecutorService;

public class NettyEventPublisher implements EventPublisher {
    private final EventAggregator eventAggregator;
    private final EventLoopGroups eventLoopGroups;

    @Inject
    NettyEventPublisher(EventAggregator eventAggregator, EventLoopGroups eventLoopGroups) {
        this.eventAggregator = eventAggregator;
        this.eventLoopGroups = eventLoopGroups;
    }

    /**
     * Publish the event on an available client thread.
     * @param event
     */
    @Override
    public void publish(final Object event) {
        getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                eventAggregator.publish(event);
            }
        });
    }

    ExecutorService getExecutor() {
        return eventLoopGroups.getClientGroup();
    }
}
