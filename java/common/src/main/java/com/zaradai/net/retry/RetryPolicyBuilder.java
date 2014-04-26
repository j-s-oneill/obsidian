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

import com.google.common.base.Preconditions;

public class RetryPolicyBuilder {
    private int attempts;
    private long millis;
    private long maxDelay;
    private boolean useExponential;

    public RetryPolicyBuilder() {
    }

    public RetryPolicyBuilder(int attempts, long millis, long maxDelay, boolean useExponential) {
        this.attempts = attempts;
        this.millis = millis;
        this.maxDelay = maxDelay;
        this.useExponential = useExponential;
    }

    public RetryPolicy build() {
        return buildRetryPolicy(buildDelayPolicy());
    }

    public RetryPolicyBuilder withAttempts(int retryAttempts) {
        attempts = retryAttempts;
        return this;
    }

    public RetryPolicyBuilder withNoRetries() {
        this.attempts = 0;
        return this;
    }

    public RetryPolicyBuilder retryForever() {
        this.attempts = -1;
        return this;
    }

    public RetryPolicyBuilder withDelayMillis(long delayInMillis) {
        millis = delayInMillis;
        return this;
    }

    public RetryPolicyBuilder withNoDelay() {
        this.millis = 0;
        return this;
    }

    public RetryPolicyBuilder withMaxDelayMillis(long delayInMillis) {
        maxDelay = delayInMillis;
        return this;
    }

    public RetryPolicyBuilder withExponentialBackOff() {
        useExponential = true;
        return this;
    }

    private RetryPolicy buildRetryPolicy(DelayPolicy delayPolicy) {
        if (attempts < 0) {
            return new AttemptForever(delayPolicy);
        } else if (attempts == 0) {
            return new AttemptOnce(delayPolicy);
        } else {
            return new AttemptNTimes(attempts, delayPolicy);
        }
    }

    private DelayPolicy buildDelayPolicy() {
        if (useExponential) {
            Preconditions.checkArgument(millis > 0, "Exponential back-off selected with invalid delay");

            if (maxDelay > 0) {
                return new BoundedExponentialBackOff(millis, maxDelay);
            } else {
                return new ExponentialBackOff(millis);
            }
        } else {
            if (millis <= 0) {
                return new NoDelay();
            } else {
                if (maxDelay > 0) {
                    return new FixedDelay(StrictMath.min(millis, maxDelay));
                } else {
                    return new FixedDelay(millis);
                }
            }
        }
    }
}
