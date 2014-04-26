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

abstract class AbstractRetryPolicy implements RetryPolicy {
    private final DelayPolicy delayPolicy;

    protected AbstractRetryPolicy(DelayPolicy delayPolicy) {
        this.delayPolicy = Preconditions.checkNotNull(delayPolicy, "Invalid delay policy specified");
    }

    public DelayPolicy getDelayPolicy() {
        return delayPolicy;
    }

    @Override
    public void reset() {
        delayPolicy.reset();
    }

    @Override
    public boolean retry() {
        if (shouldRetry()) {
            delayPolicy.delay();
            return true;
        }

        return false;
    }

    protected abstract boolean shouldRetry();
}
