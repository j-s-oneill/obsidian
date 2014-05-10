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

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaradai.config.ConfigurationSource;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.config.DistributorConfigImpl;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.events.EventAggregator;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class DistributorServiceTest {
    private Injector injector1;
    private DistributorConfig config1;
    private Injector injector2;
    private DistributorConfig config2;
    private Injector injector3;
    private DistributorConfig config3;
    private DistributorService distributorService1;
    private DistributorService distributorService2;
    private DistributorService distributorService3;

    @Before
    public void setUp() throws Exception {
        injector1 = createInjector();
        ConfigurationSource source = injector1.getInstance(ConfigurationSource.class);
        config1 = injector1.getInstance(DistributorConfig.class);
        source.set(DistributorConfigImpl.PORT, 1708);

        injector2 = createInjector();
        ConfigurationSource source2 = injector2.getInstance(ConfigurationSource.class);
        config2 = injector2.getInstance(DistributorConfig.class);
        source2.set(DistributorConfigImpl.PORT, 1709);

        injector3 = createInjector();
        ConfigurationSource source3 = injector3.getInstance(ConfigurationSource.class);
        config3 = injector3.getInstance(DistributorConfig.class);
        source3.set(DistributorConfigImpl.PORT, 1710);

        distributorService1 = injector1.getInstance(DistributorService.class);
        distributorService2 = injector2.getInstance(DistributorService.class);
        distributorService3 = injector3.getInstance(DistributorService.class);
    }

    @Test
    public void shouldRun() throws Exception {
        startDistributors();
        // sleep a bit
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        InetAddress local = InetAddress.getByName(config1.getHost());

        Message message = new Message.Builder()
                .event(new TestEvent())
                .addTarget(new InetSocketAddress(local, config2.getPort()))
                .addTarget(new InetSocketAddress(local, config3.getPort()))
                .from(new InetSocketAddress(local, config1.getPort()))
                .build();

        EventAggregator agg1 = injector1.getInstance(EventAggregator.class);
        agg1.publish(message);

        Uninterruptibles.sleepUninterruptibly(8, TimeUnit.SECONDS);

        stopDistributors();
    }

    @Test
    public void shouldRunMultiple() throws Exception {
        startDistributors();
        // sleep a bit
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        InetAddress local = InetAddress.getByName(config1.getHost());

        EventAggregator agg1 = injector1.getInstance(EventAggregator.class);

        for (int i = 0; i < 20; ++i) {
            Message message = new Message.Builder()
                    .event(new TestEvent())
                    .addTarget(new InetSocketAddress(local, config2.getPort()))
                    .addTarget(new InetSocketAddress(local, config3.getPort()))
                    .from(new InetSocketAddress(local, config1.getPort()))
                    .build();

            agg1.publish(message);
        }

        Uninterruptibles.sleepUninterruptibly(8, TimeUnit.SECONDS);

        stopDistributors();
    }


    private void stopDistributors() {
        distributorService1.stopAsync().awaitTerminated();
        distributorService2.stopAsync().awaitTerminated();
        distributorService3.stopAsync().awaitTerminated();
    }

    private void startDistributors() {
        distributorService1.startAsync().awaitRunning();
        distributorService2.startAsync().awaitRunning();
        distributorService3.startAsync().awaitRunning();
    }


    private Injector createInjector() {
        return Guice.createInjector(new DistributorModule());
    }
}
