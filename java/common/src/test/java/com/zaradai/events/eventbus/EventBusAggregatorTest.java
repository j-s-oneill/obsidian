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
package com.zaradai.events.eventbus;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventBusAggregatorTest {
    private EventBus eventBus;
    private EventBusAggregator uut;

    @Before
    public void setUp() throws Exception {
        eventBus = mock(EventBus.class);
        uut = new EventBusAggregator(eventBus);
    }

    @Test
    public void shouldPublish() throws Exception {
        Object test = new Object();

        uut.publish(test);

        verify(eventBus).post(test);
    }

    @Test
    public void shouldSubscribe() throws Exception {
        Object test = new Object();

        uut.subscribe(test);

        verify(eventBus).register(test);
    }

    @Test
    public void shouldUnsubscribe() throws Exception {
        Object test = new Object();

        uut.unsubscribe(test);

        verify(eventBus).unregister(test);
    }
}
