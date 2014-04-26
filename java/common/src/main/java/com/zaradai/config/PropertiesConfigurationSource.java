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
package com.zaradai.config;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class PropertiesConfigurationSource extends InMemoryConfigurationSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfigurationSource.class);

    @Override
    public void setup(String uri) throws ConfigurationException {
        super.setup(uri);
        Properties properties = createProperties();

        URL url = getConfigUrl(uri);
        LOGGER.info("Loading settings from {}", url);
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            throw new ConfigurationException("Unable to load properties file", e);
        }

        storeValues(properties);
        LOGGER.info("Loaded {} entries", getNumEntries());

    }

    public void save(String propertiesFileName) throws IOException {
        Properties properties = createProperties();

        for (String key : getKeys()) {
            properties.setProperty(key, get(key));
        }

        FileOutputStream stream = null;

        try {
            stream = new FileOutputStream(propertiesFileName);
            properties.store(stream, null);
            stream.flush();
        } catch (IOException e) {
            LOGGER.error("Unable to save properties file", e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }


    protected Properties createProperties() {
        return new Properties();
    }

    private void storeValues(final Properties properties) {
        if (properties.isEmpty()) {
            return;
        }

        for (final String key : properties.stringPropertyNames()) {
            final String value = properties.getProperty(key);
            if (!Strings.isNullOrEmpty(value)) {
                set(key, value);
                LOGGER.debug("{}={}", key, value);
            }
        }
    }

    private URL getConfigUrl(String uri) throws ConfigurationException {
        URL url;

        try {
            url = new URL(uri);
            url.openStream().close();
        } catch (Exception e) {
            ClassLoader loader = PropertiesConfigurationSource.class.getClassLoader();
            url = loader.getResource(uri);
            if (url == null) {
                throw new ConfigurationException("Cannot locate " + uri);
            }
        }

        return url;
    }
}
