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
package com.zaradai.net.retry;

import com.zaradai.mocks.DelayPolicyMocker;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AttemptNTimesTest {
    private static final int MAX_ATTEMPTS = 5;
    private AttemptNTimes uut;

    @Before
    public void setUp() throws Exception {
        DelayPolicy delayPolicy = DelayPolicyMocker.create();
        uut = new AttemptNTimes(MAX_ATTEMPTS, delayPolicy);
    }

    @Test
    public void shouldGetMaxAttempts() throws Exception {
        assertThat(uut.getMaxAttempts(), is(MAX_ATTEMPTS));
    }

    @Test
    public void shouldFailAfterMaxAttempts() throws Exception {
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            uut.retry();
        }

        boolean res = uut.retry();

        assertThat(res, is(false));
        assertThat(uut.getAttempts(), is(MAX_ATTEMPTS));

    }

    @Test
    public void shouldNotFailIfAttemptLessThanMaxAttempts() throws Exception {
        boolean res = uut.retry();

        assertThat(res, is(true));
        assertThat(uut.getAttempts(), is(1));
    }

    @Test
    public void shouldResetCountAfterReset() throws Exception {
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            uut.retry();
        }
        assertThat(uut.retry(), is(false));

        uut.reset();

        assertThat(uut.retry(), is(true));
    }
}
