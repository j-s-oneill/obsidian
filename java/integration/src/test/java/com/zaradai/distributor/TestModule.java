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

import com.google.inject.AbstractModule;
import com.zaradai.app.ApplicationService;
import com.zaradai.config.ConfigurationSource;
import com.zaradai.config.InMemoryConfigurationSource;
import com.zaradai.config.PropertiesConfigurationSource;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.config.DistributorConfigImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestModule extends DistributorModule {
    private final ExecutorService executorService;

    public TestModule() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void configure() {
        super.configure();
        // add test interfaces
        bind(ApplicationService.class).to(TestApplicationService.class);
        // bind an executor service to run the testing code on
        bind(ExecutorService.class).toInstance(executorService);
    }

    @Override
    protected void bindConfig() {
        // override and setup InMemory for the test
        bind(ConfigurationSource.class).to(InMemoryConfigurationSource.class).asEagerSingleton();
        bind(DistributorConfig.class).to(DistributorConfigImpl.class);
    }
}
