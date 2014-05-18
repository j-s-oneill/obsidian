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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaradai.config.ConfigurationSource;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.config.DistributorConfigImpl;
import com.zaradai.events.EventAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDistributorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDistributorService.class);

    private final Injector injector;
    private final ConfigurationSource source;
    private final DistributorService distributorService;
    private final DistributorConfig config;
    private final EventAggregator eventAggregator;

    public SimpleDistributorService() {
        injector = createInjector();
        source = injector.getInstance(ConfigurationSource.class);
        distributorService = injector.getInstance(DistributorService.class);
        config = injector.getInstance(DistributorConfig.class);
        eventAggregator = injector.getInstance(EventAggregator.class);
    }

    public void setPort(int port) {
        source.set(DistributorConfigImpl.PORT, port);
    }

    public int getPort() {
        return config.getPort();
    }

    public void setHost(String host) {
        source.set(DistributorConfigImpl.HOST, host);
    }

    public String getHost() {
        return config.getHost();
    }

    public PingPongTester getTester() {
        return injector.getInstance(PingPongTester.class);
    }

    public void start() throws Exception {
        distributorService.startAsync().awaitRunning();
    }

    public void stop() throws Exception {
        distributorService.stopAsync().awaitTerminated();
    }

    private Injector createInjector() {
        return Guice.createInjector(new DistributorModule(), new TestModule());
    }
}
