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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public class InMemoryConfigurationSourceTest {
    private static final String TEST_KEY = "key";
    private static final String TEST_STRING = "test";
    @Mock
    Map<String, String> mockMap;

    InMemoryConfigurationSource uut;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        uut = new InMemoryConfigurationSource() {
            @Override
            protected Map<String, String> createConfigMap() {
                return mockMap;
            }
        };
    }

    @Test
    public void shouldAddToMapOnSet() throws Exception {
        uut.set(TEST_KEY, TEST_STRING);

        verify(mockMap).put(TEST_KEY, TEST_STRING);
    }

    @Test
    public void shouldGetFromMapOnGet() throws Exception {
        uut.get(TEST_KEY);

        verify(mockMap).get(TEST_KEY);
    }

    @Test
    public void shouldGetWhenInMap() throws Exception {
        uut = new InMemoryConfigurationSource();

        uut.set(TEST_KEY, TEST_STRING);

        String res = uut.get(TEST_KEY);

        assertThat(res, is(TEST_STRING));
    }

    @Test
    public void shouldGetNumEntries() throws Exception {
        final int NUM_ENTRIES = 5;
        uut = new InMemoryConfigurationSource();

        for (int i = 0; i < NUM_ENTRIES; ++i) {
            uut.set(TEST_KEY+i, TEST_STRING);
        }

        assertThat(uut.getNumEntries(), is(NUM_ENTRIES));
    }

    @Test
    public void shouldGetKeys() throws Exception {
        uut = new InMemoryConfigurationSource();

        uut.set(TEST_KEY, TEST_STRING);

        Set<String> keys = uut.getKeys();

        assertThat(keys.isEmpty(), is(false));
        assertThat(keys.contains(TEST_KEY), is(true));
    }
}
