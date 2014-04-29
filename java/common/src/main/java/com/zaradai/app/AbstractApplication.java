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
package com.zaradai.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.zaradai.config.ConfigurationException;
import com.zaradai.config.ConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApplication.class);
    private Injector injector;

    public final void run() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(loggerContext);

        try {
            addShutdownHook();
            loadConfiguration();
            initialize();
            runApplication();
            shutdown(false);
        } catch (Exception e) {
            LOGGER.error("Application Failure", e);
        }
    }

    private void addShutdownHook() {
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutdown hook executing...");
                try {
                    shutdown(true);
                    mainThread.join();
                } catch (Exception e) {
                    LOGGER.error("Application failure in shutdown hook", e);
                }
            }
        });
    }

    private void loadConfiguration() throws ConfigurationException {
        ConfigurationSource source = getConfigurationSource();
        source.setup(getPropertiesPath());
    }

    protected String getPropertiesPath() {
        return "";
    }

    protected ConfigurationSource getConfigurationSource() {
        return getInjector().getInstance(ConfigurationSource.class);
    }

    protected void initialize() {

    }

    private void runApplication() {
        ApplicationService applicationService = getInjector().getInstance(ApplicationService.class);
        applicationService.startAsync().awaitTerminated();
    }

    protected Injector getInjector() {
        if (injector == null) {
            injector = createInjector();
        }

        return injector;
    }

    private Injector createInjector() {
        return Guice.createInjector(getModules());
    }

    protected abstract Iterable<? extends Module> getModules();

    protected void shutdown(boolean fromShutdownHook) {

    }
}
