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
import com.zaradai.util.SleepDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

abstract class AbstractDelayPolicy implements DelayPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayPolicy.class);

    private final Delay delay;

    protected AbstractDelayPolicy() {
        this.delay = createDelay();
    }

    protected Delay createDelay() {
        return new SleepDelay();
    }

    @Override
    public void reset() {
        //NOP
    }

    @Override
    public void delay() {
        long delayInMillis = getDelayPeriodInMillis();
        LOGGER.debug("Retry in {} ms", delayInMillis);
        delay.delay(delayInMillis, TimeUnit.MILLISECONDS, false);
    }

    protected abstract long getDelayPeriodInMillis();
}
