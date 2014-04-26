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

import com.zaradai.mocks.DelayMocker;
import com.zaradai.mocks.RandomGeneratorMocker;
import com.zaradai.util.Delay;
import com.zaradai.util.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class ExponentialBackOffTest {
    private static final long BASE_DELAY = 100;
    private static final int TEST_RANDOM = 8;

    private Delay delay;
    private RandomGenerator randomGenerator;
    private ExponentialBackOff uut;

    @Before
    public void setUp() throws Exception {
        delay = DelayMocker.create();
        randomGenerator = RandomGeneratorMocker.create();
        when(randomGenerator.nextInt(anyInt())).thenReturn(TEST_RANDOM);

        uut = new ExponentialBackOff(BASE_DELAY) {
            @Override
            protected RandomGenerator createRandom() {
                return randomGenerator;
            }

            @Override
            protected Delay createDelay() {
                return delay;
            }
        };
    }

    @Test
    public void shouldCalculateDelayUsingAlgo() throws Exception {
        uut.delay();

        long calculated = BASE_DELAY * TEST_RANDOM;
        verify(delay).delay(calculated, TimeUnit.MILLISECONDS, false);
    }

    @Test
    public void shouldIncreaseExponentiallyRandomMaxForEachAttempt() throws Exception {
        uut.delay();
        uut.delay();
        uut.delay();
        uut.delay();

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(randomGenerator, atLeastOnce()).nextInt(captor.capture());
        List<Integer> calls = captor.getAllValues();

        assertThat(calls.size(), is(4));
        assertThat(calls.get(0), is(2));
        assertThat(calls.get(1), is(4));
        assertThat(calls.get(2), is(8));
        assertThat(calls.get(3), is(16));
    }

    @Test
    public void shouldReset() throws Exception {
        uut.delay();
        uut.delay();
        uut.reset();    // reset now
        uut.delay();
        uut.delay();

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(randomGenerator, atLeastOnce()).nextInt(captor.capture());
        List<Integer> calls = captor.getAllValues();

        assertThat(calls.size(), is(4));
        assertThat(calls.get(0), is(2));
        assertThat(calls.get(1), is(4));
        // reset is here
        assertThat(calls.get(2), is(2));
        assertThat(calls.get(3), is(4));
    }
}
