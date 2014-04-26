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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationExceptionTest {
    private static final String TEST_MESSAGE = "test";
    private static final Throwable TEST_CAUSE = new Exception();

    @Test
    public void shouldCreateWithMessage() throws Exception {
        ConfigurationException uut = new ConfigurationException(TEST_MESSAGE);

        assertThat(uut.getMessage(), is(TEST_MESSAGE));
    }

    @Test
    public void shouldCreateWithMessageAndCause() throws Exception {
        ConfigurationException uut = new ConfigurationException(TEST_MESSAGE, TEST_CAUSE);

        assertThat(uut.getMessage(), is(TEST_MESSAGE));
        assertThat(uut.getCause(), is(TEST_CAUSE));
    }
}
