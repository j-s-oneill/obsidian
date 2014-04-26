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
import com.zaradai.util.Delay;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;

public class FixedDelayTest {
    private static final long TEST_DELAY = 42;

    @Test
    public void shouldGetDelayFromPeriodInMillis() throws Exception {
        final Delay delay = DelayMocker.create();
        FixedDelay uut = new FixedDelay(TEST_DELAY) {
            @Override
            protected Delay createDelay() {
                return delay;
            }
        };

        uut.delay();

        verify(delay).delay(TEST_DELAY, TimeUnit.MILLISECONDS, false);
    }
}
