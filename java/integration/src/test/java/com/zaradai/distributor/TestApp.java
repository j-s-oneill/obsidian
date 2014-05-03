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

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.zaradai.app.AbstractApplication;
import com.zaradai.config.ConfigurationSource;
import com.zaradai.distributor.config.DistributorConfigImpl;
import com.zaradai.events.EventAggregator;

import java.util.concurrent.ExecutorService;

public class TestApp extends AbstractApplication {
    private final int port;

    public TestApp(int port) {
        this.port = port;
    }

    public void postEvent(Object event) {
        getInjector().getInstance(EventAggregator.class).publish(event);
    }

    @Override
    protected void initialize() {
        // setup the config properties
        ConfigurationSource configurationSource = getConfigurationSource();
        // use defaults for all except port.
        configurationSource.set(DistributorConfigImpl.PORT, port);
    }

    @Override
    protected Iterable<? extends Module> getModules() {
        return Lists.newArrayList(new TestModule());
    }

    @Override
    protected void shutdown(boolean fromShutdownHook) {
        super.shutdown(fromShutdownHook);
        // shutdown the executor service we use for the tests
        getInjector().getInstance(ExecutorService.class).shutdown();
    }
}
