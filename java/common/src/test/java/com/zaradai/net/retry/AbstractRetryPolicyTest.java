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
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public class AbstractRetryPolicyTest {
    @Test
    public void shouldDelayAndReturnTrueIfShouldRetry() throws Exception {
        DelayPolicy delayPolicy = DelayPolicyMocker.create();
        AbstractRetryPolicy uut = new AbstractRetryPolicy(delayPolicy) {
            @Override
            protected boolean shouldRetry() {
                return true;
            }
        };

        boolean res = uut.retry();

        assertThat(res, is(true));
        verify(delayPolicy).delay();
    }

    @Test
    public void shouldResetDelayOnReset() throws Exception {
        DelayPolicy delayPolicy = DelayPolicyMocker.create();
        AbstractRetryPolicy uut = new AbstractRetryPolicy(delayPolicy) {
            @Override
            protected boolean shouldRetry() {
                return true;
            }
        };

        uut.reset();

        verify(delayPolicy).reset();
    }

    @Test
    public void shouldReturnFalseIfNoRetry() throws Exception {
        DelayPolicy delayPolicy = DelayPolicyMocker.create();
        AbstractRetryPolicy uut = new AbstractRetryPolicy(delayPolicy) {
            @Override
            protected boolean shouldRetry() {
                return false;
            }
        };

        boolean res = uut.retry();

        assertThat(res, is(false));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfInvalidDelayPolicy() throws Exception {
        new AbstractRetryPolicy(null) {
            @Override
            protected boolean shouldRetry() {
                return false;
            }
        };
    }
}
