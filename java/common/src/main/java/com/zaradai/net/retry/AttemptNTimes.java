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

public class AttemptNTimes extends AbstractRetryPolicy {
    private final int maxAttempts;
    private int attempts;

    public AttemptNTimes(int maxAttempts, DelayPolicy delayPolicy) {
        super(delayPolicy);
        Preconditions.checkArgument(maxAttempts > 0, "Invalid Max Attempts");
        this.maxAttempts = maxAttempts;
        attempts = 0;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getAttempts() {
        return attempts;
    }

    @Override
    public void reset() {
        super.reset();
        attempts = 0;
    }

    @Override
    protected boolean shouldRetry() {
        if (attempts < maxAttempts) {
            // increase the count
            attempts++;
            return true;
        }

        return false;
    }
}
