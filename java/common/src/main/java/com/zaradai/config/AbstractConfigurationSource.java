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

/**
 * Provides a default implementation for {link ConfigurationSource }.
 * Default action is to try and type coerce given String representation and catch
 * any errors in the conversion.  If no value is found or the type cant be coerced then
 * a provided default value is returned.
 */
public abstract class AbstractConfigurationSource implements ConfigurationSource {
    @Override
    public void setup(String uri) throws ConfigurationException {
        if (uri == null) {
            throw new ConfigurationException("Invalid URI");
        }
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        String value = get(key);

        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    @Override
    public int get(String key, int defaultValue) {
        String value = get(key);

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public long get(String key, long defaultValue) {
        String value = get(key);

        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public float get(String key, float defaultValue) {
        String value = get(key);

        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public double get(String key, double defaultValue) {
        String value = get(key);

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);

        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public void set(String key, boolean value) {
        set(key, Boolean.toString(value));
    }

    @Override
    public void set(String key, int value) {
        set(key, Integer.toString(value));
    }

    @Override
    public void set(String key, long value) {
        set(key, Long.toString(value));
    }

    @Override
    public void set(String key, float value) {
        set(key, Float.toString(value));
    }

    @Override
    public void set(String key, double value) {
        set(key, Double.toString(value));
    }
}
