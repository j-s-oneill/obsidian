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
package com.zaradai.distributor.events;

import com.zaradai.distributor.messaging.Message;
import com.zaradai.mocks.MessageMocker;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MessageSentEventTest {
    private static final Message TEST_MESSAGE = MessageMocker.create();

    @Test
    public void shouldCreateWithMessage() throws Exception {
        MessageSentEvent uut = new MessageSentEvent(TEST_MESSAGE);

        assertThat(uut.getMessage(), is(TEST_MESSAGE));
    }

    @Test
    public void shouldGetMessage() throws Exception {

        MessageSentEvent uut = new MessageSentEvent();

        uut.setMessage(TEST_MESSAGE);

        assertThat(uut.getMessage(), is(TEST_MESSAGE));
    }
}
