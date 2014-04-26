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

import com.zaradai.util.RandomGenerator;
import com.zaradai.util.SystemRandomGenerator;

public class ExponentialBackOff extends AbstractDelayPolicy {
    private static final int MAX_ATTEMPTS = 32;

    private final long baseDelayInMillis;
    private final RandomGenerator random;
    private int attempt;

    public ExponentialBackOff(long baseDelayInMillis) {
        this.baseDelayInMillis = baseDelayInMillis;
        random = createRandom();
    }

    protected RandomGenerator createRandom() {
        return new SystemRandomGenerator();
    }

    @Override
    public void reset() {
        super.reset();
        attempt = 0;
    }

    @Override
    protected long getDelayPeriodInMillis() {
        return recalculateDelay();
    }

    private long recalculateDelay() {
        attempt++;

        if (attempt > MAX_ATTEMPTS) {
            attempt = MAX_ATTEMPTS;
        }

        return baseDelayInMillis * Math.max(1, random.nextInt(1 << attempt));
    }
}
