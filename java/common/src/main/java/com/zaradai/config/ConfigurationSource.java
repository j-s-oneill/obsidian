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

/**
 * Provides a key value store to set and retrieve configuration data.
 * Helper functions provided to do common type conversions.
 */
public interface ConfigurationSource {
    void setup(String uri) throws ConfigurationException;

    /**
     * Get string configuration value to associated key.
     * @param key to lookup value.
     * @return config value or null if not found.
     */
    String get(String key);

    /**
     * Set a string value.  Entries with same key will be overridden with
     * latest value.
     * @param key to insert value into.
     * @param value to insert.
     */
    void set(String key, String value);

    /**
     * Gets a boolean value returning provided default if value does not exist.
     * @param key to lookup value.
     * @param defaultValue to set if not found using key.
     * @return value or default.
     */
    boolean get(String key, boolean defaultValue);

    /**
     * Gets an integer value returning provided default if value does
     * not exist.
     * @param key to lookup value.
     * @param defaultValue to set if not found using key.
     * @return value or default.
     */
    int get(String key, int defaultValue);

    /**
     * Gets a long value returning provided default if value does
     * not exist.
     * @param key to lookup value.
     * @param defaultValue to set if not found using key.
     * @return value or default.
     */
    long get(String key, long defaultValue);
    /**
     * Gets a float value returning provided default if value does
     * not exist.
     * @param key to lookup value.
     * @param defaultValue to set if not found using key.
     * @return value or default.
     */
    float get(String key, float defaultValue);
    /**
     * Gets a double value returning provided default if value does
     * not exist.
     * @param key to lookup value.
     * @param defaultValue to set if not found using key.
     * @return value or default.
     */
    double get(String key, double defaultValue);
    /**
     * Gets a String value returning provided default if value does
     * not exist.
     * @param key to lookup value.
     * @param defaultValue to set if not found using key.
     * @return value or default.
     */
    String get(String key, String defaultValue);

    /**
     * Sets a boolean value for the given key.
     * @param key to insert value under.
     * @param value to store.
     */
    void set(String key, boolean value);
    /**
     * Sets an int value for the given key.
     * @param key to insert value under.
     * @param value to store.
     */
    void set(String key, int value);
    /**
     * Sets a long value for the given key.
     * @param key to insert value under.
     * @param value to store.
     */
    void set(String key, long value);
    /**
     * Sets a float value for the given key.
     * @param key to insert value under.
     * @param value to store.
     */
    void set(String key, float value);
    /**
     * Sets a double value for the given key.
     * @param key to insert value under.
     * @param value to store.
     */
    void set(String key, double value);
}
