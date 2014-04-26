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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class RetryPolicyBuilderTest {
    private final int TEST_ATTEMPTS = 5;

    @Test
    public void shouldBuildAttemptOnceWithDefault() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder().build();

        assertThat(policy, instanceOf(AttemptOnce.class));
    }

    @Test
    public void shouldBuildAttemptOnceUsingNoRetries() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withNoRetries()
                .build();

        assertThat(policy, instanceOf(AttemptOnce.class));
    }

    @Test
    public void shouldBuildAttemptOnceUsing0Attempts() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(0)
                .build();

        assertThat(policy, instanceOf(AttemptOnce.class));
    }

    @Test
    public void shouldBuildNTimes() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;

        assertThat(res, not(nullValue()));
        assertThat(res.getMaxAttempts(), is(TEST_ATTEMPTS));
    }

    @Test
    public void shouldBuildAttemptForeverUsingNegativeAttempts() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(-1)
                .build();

        assertThat(policy, instanceOf(AttemptForever.class));
    }

    @Test
    public void shouldBuildAttemptForeverWithRetryForever() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .retryForever()
                .build();

        assertThat(policy, instanceOf(AttemptForever.class));
    }

    @Test
    public void shouldBuildNTimesWithNoDelayUsingNoDelay() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withNoDelay()
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;

        assertThat(res.getDelayPolicy(), instanceOf(NoDelay.class));
    }

    @Test
    public void shouldBuildNTimesWithNoDelayUsingDelayMillisOf0() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withDelayMillis(0)
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;

        assertThat(res.getDelayPolicy(), instanceOf(NoDelay.class));
    }

    @Test
    public void shouldBuildNTimesWithNoDelayUsingDelayMillisLessThan0() throws Exception {
        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withDelayMillis(-1)
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;

        assertThat(res.getDelayPolicy(), instanceOf(NoDelay.class));
    }

    @Test
    public void shouldBuildNTimesWithFixedDelayNotMoreThanMax() throws Exception {
        final long FIXED_DELAY = 5000L;
        final long MAX_DELAY = 1000L;

        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withDelayMillis(FIXED_DELAY)
                .withMaxDelayMillis(MAX_DELAY)
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;
        assertThat(res.getDelayPolicy(), instanceOf(FixedDelay.class));

        FixedDelay delay = (FixedDelay) res.getDelayPolicy();
        assertThat(delay.getDelayPeriodInMillis(), is(MAX_DELAY));
    }

    @Test
    public void shouldBuildNTimesWithFixedDelayIfLessThanMax() throws Exception {
        final long FIXED_DELAY = 5000L;
        final long MAX_DELAY = 75000L;

        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withDelayMillis(FIXED_DELAY)
                .withMaxDelayMillis(MAX_DELAY)
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;
        assertThat(res.getDelayPolicy(), instanceOf(FixedDelay.class));

        FixedDelay delay = (FixedDelay) res.getDelayPolicy();
        assertThat(delay.getDelayPeriodInMillis(), is(FIXED_DELAY));
    }

    @Test
    public void shouldBuildNTimesWithExponentialBackOff() throws Exception {
        final long FIXED_DELAY = 5000L;

        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withDelayMillis(FIXED_DELAY)
                .withExponentialBackOff()
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;
        assertThat(res.getDelayPolicy(), instanceOf(ExponentialBackOff.class));

        ExponentialBackOff delay = (ExponentialBackOff) res.getDelayPolicy();
        assertThat(delay.getDelayPeriodInMillis(), is(FIXED_DELAY));
    }

    @Test
    public void shouldBuildNTimesWithBoundedExponentialBackOff() throws Exception {
        final long FIXED_DELAY = 5000L;
        final long MAX_DELAY = 75000L;

        RetryPolicy policy = new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withDelayMillis(FIXED_DELAY)
                .withMaxDelayMillis(MAX_DELAY)
                .withExponentialBackOff()
                .build();

        AttemptNTimes res = (AttemptNTimes)policy;
        assertThat(res.getDelayPolicy(), instanceOf(ExponentialBackOff.class));

        BoundedExponentialBackOff delay = (BoundedExponentialBackOff) res.getDelayPolicy();
        assertThat(delay.getDelayPeriodInMillis(), is(FIXED_DELAY));
        assertThat(delay.getMaxDelayInMillis(), is(MAX_DELAY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfBackOffBuiltWithInvalidInterval() throws Exception {

        new RetryPolicyBuilder()
                .withAttempts(TEST_ATTEMPTS)
                .withExponentialBackOff()
                .build();
    }

    @Test
    public void shouldBuildAttemptOnceUsingConstructorOnly() throws Exception {
        int attempts = 0;
        long interval = 0;
        long maxDelay = 0;
        boolean useExponential = false;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        assertThat(policy, instanceOf(AttemptOnce.class));
    }

    @Test
    public void shouldBuildAttemptForeverUsingConstructorOnly() throws Exception {
        int attempts = -1;
        long interval = 0;
        long maxDelay = 0;
        boolean useExponential = false;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        assertThat(policy, instanceOf(AttemptForever.class));
    }

    @Test
    public void shouldBuildAttemptNTimesUsingConstructorOnly() throws Exception {
        int attempts = 5;
        long interval = 0;
        long maxDelay = 0;
        boolean useExponential = false;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        assertThat(policy, instanceOf(AttemptNTimes.class));
        assertThat(((AttemptNTimes)policy).getMaxAttempts(), is(attempts));
    }

    @Test
    public void shouldBuildFixedDelayUsingConstructorOnly() throws Exception {
        int attempts = 5;
        long interval = 1000;
        long maxDelay = 0;
        boolean useExponential = false;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        assertThat(policy, instanceOf(AttemptNTimes.class));
        AttemptNTimes nTimes = (AttemptNTimes) policy;
        assertThat(nTimes.getDelayPolicy(), instanceOf(FixedDelay.class));
        assertThat(((FixedDelay)nTimes.getDelayPolicy()).getDelayPeriodInMillis(), is(interval));
    }

    @Test
    public void shouldBuildNoDelayUsingConstructorOnly() throws Exception {
        int attempts = 5;
        long interval = 0;
        long maxDelay = 0;
        boolean useExponential = false;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        AttemptNTimes nTimes = (AttemptNTimes) policy;
        assertThat(nTimes.getDelayPolicy(), instanceOf(NoDelay.class));
    }

    @Test
    public void shouldBuildExponentialDelayUsingConstructorOnly() throws Exception {
        int attempts = 5;
        long interval = 1000;
        long maxDelay = 0;
        boolean useExponential = true;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        AttemptNTimes nTimes = (AttemptNTimes) policy;
        assertThat(nTimes.getDelayPolicy(), instanceOf(ExponentialBackOff.class));
        assertThat(((ExponentialBackOff)nTimes.getDelayPolicy()).getDelayPeriodInMillis(), is(interval));
    }

    @Test
    public void shouldBuildBoundedExponentialDelayUsingConstructorOnly() throws Exception {
        int attempts = 5;
        long interval = 1000;
        long maxDelay = 5000;
        boolean useExponential = true;

        RetryPolicy policy = new RetryPolicyBuilder(attempts, interval, maxDelay, useExponential).build();

        AttemptNTimes nTimes = (AttemptNTimes) policy;
        assertThat(nTimes.getDelayPolicy(), instanceOf(BoundedExponentialBackOff.class));
        assertThat(((BoundedExponentialBackOff)nTimes.getDelayPolicy()).getDelayPeriodInMillis(), is(interval));
        assertThat(((BoundedExponentialBackOff)nTimes.getDelayPolicy()).getMaxDelayInMillis(), is(maxDelay));
    }
}
