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
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class DistributorServiceTest {
    @Test
    public void shouldRun() throws Exception {
        Injector injector1 = createInjector();
        ConfigurationSource source = injector1.getInstance(ConfigurationSource.class);
        DistributorConfig config1 = injector1.getInstance(DistributorConfig.class);
        source.set(DistributorConfigImpl.PORT, 1708);

        Injector injector2 = createInjector();
        ConfigurationSource source2 = injector2.getInstance(ConfigurationSource.class);
        DistributorConfig config2 = injector2.getInstance(DistributorConfig.class);
        source2.set(DistributorConfigImpl.PORT, 1709);

        Injector injector3 = createInjector();
        ConfigurationSource source3 = injector3.getInstance(ConfigurationSource.class);
        DistributorConfig config3 = injector3.getInstance(DistributorConfig.class);
        source3.set(DistributorConfigImpl.PORT, 1710);

        DistributorService distributorService1 = injector1.getInstance(DistributorService.class);
        distributorService1.startAsync().awaitRunning();

        DistributorService distributorService2 = injector2.getInstance(DistributorService.class);
        distributorService2.startAsync().awaitRunning();

        DistributorService distributorService3 = injector3.getInstance(DistributorService.class);
        distributorService3.startAsync().awaitRunning();

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

        Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);

        distributorService1.stopAsync().awaitTerminated();
        distributorService2.stopAsync().awaitTerminated();
        distributorService3.stopAsync().awaitTerminated();
    }


    private Injector createInjector() {
        return Guice.createInjector(new DistributorModule());
    }
}
