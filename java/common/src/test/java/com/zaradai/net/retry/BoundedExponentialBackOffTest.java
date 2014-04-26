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

import com.zaradai.util.Delay;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BoundedExponentialBackOffTest {
    private static final long BASE_DELAY = 100;
    private static final long BOUNDED_DELAY = 32000;

    @Test
    public void shouldMaxOutAtBoundedDelay() throws Exception {
        final int ITERATION = 30;
        final Delay delay = mock(Delay.class);
        BoundedExponentialBackOff uut = new BoundedExponentialBackOff(BASE_DELAY, BOUNDED_DELAY) {
            @Override
            protected Delay createDelay() {
                return delay;
            }
        };

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        for (int i = 0; i < ITERATION; ++i) {
            uut.delay();
        }

        verify(delay, atLeastOnce()).delay(captor.capture(), any(TimeUnit.class), anyBoolean());

        List<Long> delays = captor.getAllValues();

        assertThat(delays.size(), is(ITERATION));
        assertThat(delays.get(ITERATION-1), is(BOUNDED_DELAY));
    }
}
