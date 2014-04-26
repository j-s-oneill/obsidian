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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AbstractConfigurationSourceTest {
    private static final String TEST_KEY = "test";

    private static final boolean DEFAULT_BOOLEAN = true;
    private static final String TEST_BOOLEAN_AS_STRING = "true";
    private static final boolean TEST_BOOLEAN = true;

    private static final int DEFAULT_INT = 42;
    private static final int TEST_INT = 26;
    private static final String TEST_INT_AS_STRING = "26";

    private static final long DEFAULT_LONG = 42l;
    private static final long TEST_LONG = 26L;
    private static final String TEST_LONG_AS_STRING = "26";


    private static final float DEFAULT_FLOAT = 42.0f;
    private static final float TEST_FLOAT = 26.26f;
    private static final String TEST_FLOAT_AS_STRING = "26.26";


    private static final double DEFAULT_DOUBLE = 42.0;
    private static final double TEST_DOUBLE = 26.26;
    private static final String TEST_DOUBLE_AS_STRING = "26.26";

    private static final String DEFAULT_STRING = "test";
    private static final String TEST_STRING = "the string";


    private AbstractConfigurationSource uut;

    private String getKey;
    private String getValue;

    private String setKey;
    private String setValue;


    @Before
    public void setUp() throws Exception {
        getKey = getValue = setKey = setValue = null;

        uut = new AbstractConfigurationSource() {
            @Override
            public String get(String key) {
                getKey = key;

                return getValue;
            }

            @Override
            public void set(String key, String value) {
                setKey = key;
                setValue = value;
            }
        };
    }

    @Test(expected = ConfigurationException.class)
    public void shouldCatchNullInSetup() throws Exception {
        uut.setup(null);
    }

    @Test
    public void shouldNotThrowIfSetupWithValue() throws Exception {
        uut.setup("test");
    }

    @Test
    public void shouldGetBoolean() throws Exception {
        getValue = TEST_BOOLEAN_AS_STRING;

        boolean res = uut.get(TEST_KEY, DEFAULT_BOOLEAN);

        assertThat(res, is(TEST_BOOLEAN));
        assertThat(getKey, is(TEST_KEY));
    }

    @Test
    public void shouldGetDefaultIfBooleanValueIsNull() throws Exception {
        boolean res = uut.get(TEST_KEY, DEFAULT_BOOLEAN);

        assertThat(res, is(DEFAULT_BOOLEAN));
    }

    @Test
    public void shouldGetInteger() throws Exception {
        getValue = TEST_INT_AS_STRING;

        int res = uut.get(TEST_KEY, DEFAULT_INT);

        assertThat(res, is(TEST_INT));
        assertThat(getKey, is(TEST_KEY));
    }

    @Test
    public void shouldGetDefaultIfIntValueIsNull() throws Exception {
        int res = uut.get(TEST_KEY, DEFAULT_INT);

        assertThat(res, is(DEFAULT_INT));
    }

    @Test
    public void shouldGetLong() throws Exception {
        getValue = TEST_LONG_AS_STRING;

        long res = uut.get(TEST_KEY, DEFAULT_LONG);

        assertThat(res, is(TEST_LONG));
        assertThat(getKey, is(TEST_KEY));
    }

    @Test
    public void shouldGetDefaultIfLongValueIsNull() throws Exception {
        long res = uut.get(TEST_KEY, DEFAULT_LONG);

        assertThat(res, is(DEFAULT_LONG));
    }

    @Test
    public void shouldGetFloat() throws Exception {
        getValue = TEST_FLOAT_AS_STRING;

        float res = uut.get(TEST_KEY, DEFAULT_FLOAT);

        assertThat(res, is(TEST_FLOAT));
        assertThat(getKey, is(TEST_KEY));
    }

    @Test
    public void shouldGetDefaultIfFloatValueIsNull() throws Exception {
        float res = uut.get(TEST_KEY, DEFAULT_FLOAT);

        assertThat(res, is(DEFAULT_FLOAT));
    }

    @Test
    public void shouldGetDouble() throws Exception {
        getValue = TEST_DOUBLE_AS_STRING;

        double res = uut.get(TEST_KEY, DEFAULT_DOUBLE);

        assertThat(res, is(TEST_DOUBLE));
        assertThat(getKey, is(TEST_KEY));
    }

    @Test
    public void shouldGetDefaultIfDoubleValueIsNull() throws Exception {
        double res = uut.get(TEST_KEY, DEFAULT_DOUBLE);

        assertThat(res, is(DEFAULT_DOUBLE));
    }

    @Test
    public void shouldGetString() throws Exception {
        getValue = TEST_STRING;

        String res = uut.get(TEST_KEY, DEFAULT_STRING);

        assertThat(res, is(TEST_STRING));
        assertThat(getKey, is(TEST_KEY));
    }

    @Test
    public void shouldGetDefaultStringIfValueIsNull() throws Exception {
        getValue = null;

        String res = uut.get(TEST_KEY, DEFAULT_STRING);

        assertThat(res, is(DEFAULT_STRING));
    }

    @Test
    public void shouldGetDefaultStringIfValueIsEmpty() throws Exception {
        getValue = "";

        String res = uut.get(TEST_KEY, DEFAULT_STRING);

        assertThat(res, is(DEFAULT_STRING));
    }

    @Test
    public void shouldSetBoolean() throws Exception {
        uut.set(TEST_KEY, TEST_BOOLEAN);

        assertThat(setKey, is(TEST_KEY));
        assertThat(setValue, is(TEST_BOOLEAN_AS_STRING));
    }

    @Test
    public void shouldSetInt() throws Exception {
        uut.set(TEST_KEY, TEST_INT);

        assertThat(setKey, is(TEST_KEY));
        assertThat(setValue, is(TEST_INT_AS_STRING));
    }

    @Test
    public void shouldSetLong() throws Exception {
        uut.set(TEST_KEY, TEST_LONG);

        assertThat(setKey, is(TEST_KEY));
        assertThat(setValue, is(TEST_LONG_AS_STRING));
    }

    @Test
    public void shouldSetFloat() throws Exception {
        uut.set(TEST_KEY, TEST_FLOAT);

        assertThat(setKey, is(TEST_KEY));
        assertThat(setValue, is(TEST_FLOAT_AS_STRING));
    }

    @Test
    public void shouldSetDouble() throws Exception {
        uut.set(TEST_KEY, TEST_DOUBLE);

        assertThat(setKey, is(TEST_KEY));
        assertThat(setValue, is(TEST_DOUBLE_AS_STRING));
    }
}
