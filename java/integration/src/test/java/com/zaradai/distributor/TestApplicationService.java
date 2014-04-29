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
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.zaradai.app.ApplicationService;
import com.zaradai.distributor.events.ShutdownServiceEvent;
import com.zaradai.events.EventAggregator;
import com.zaradai.util.Delay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class TestApplicationService extends AbstractIdleService implements ApplicationService{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestApplicationService.class);
    public static final int SLEEP_FOR = 5;

    private final DistributorService distributorService;
    private final ExecutorService executorService;

    @Inject
    TestApplicationService(EventAggregator eventAggregator, DistributorService distributorService,
                           ExecutorService executorService) {
        this.distributorService = distributorService;
        this.executorService = executorService;
        eventAggregator.subscribe(this);
    }

    @Override
    protected void startUp() throws Exception {
        distributorService.startAsync().awaitRunning();
        // for now shutdown after 5 secs
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Uninterruptibles.sleepUninterruptibly(SLEEP_FOR, TimeUnit.SECONDS);
                onShutdown(new ShutdownServiceEvent());
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
        distributorService.stopAsync().awaitTerminated();
    }

    @Subscribe
    public void onShutdown(ShutdownServiceEvent event) {
        LOGGER.debug("Shutdown event called...");
        stopAsync();
    }
}
