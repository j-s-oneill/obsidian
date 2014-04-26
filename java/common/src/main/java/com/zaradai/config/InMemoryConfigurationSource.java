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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

public class InMemoryConfigurationSource extends AbstractConfigurationSource {
    private final Map<String, String> configurationData;

    public InMemoryConfigurationSource() {
        configurationData = createConfigMap();
    }

    protected Map<String, String> createConfigMap() {
        return Maps.newConcurrentMap();
    }

    @Override
    public String get(String key) {
        return configurationData.get(key);
    }

    @Override
    public void set(String key, String value) {
        configurationData.put(key, value);
    }

    protected int getNumEntries() {
        return configurationData.size();
    }

    protected Set<String> getKeys() {
        return ImmutableSet.copyOf(configurationData.keySet());
    }
}
