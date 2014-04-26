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
package com.zaradai.distributor.messaging;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class MessageTest {
    private static final UUID TEST_ID = UUID.randomUUID();
    private static final InetSocketAddress TEST_SOURCE = mock(InetSocketAddress.class);
    private static final Object TEST_EVENT = new Object();
    private static final boolean TEST_INCOMING = true;
    private static final InetSocketAddress TEST_TARGET = mock(InetSocketAddress.class);

    @Test
    public void shouldCreateWithAnEvent() throws Exception {
        Message uut = new Message(TEST_EVENT);

        assertThat(uut.getEvent(), is(TEST_EVENT));
    }

    @Test
    public void shouldGetId() throws Exception {
        Message uut = new Message();
        uut.setId(TEST_ID);

        assertThat(uut.getId(), is(TEST_ID));
    }

    @Test
    public void shouldGetSource() throws Exception {
        Message uut = new Message();
        uut.setSource(TEST_SOURCE);

        assertThat(uut.getSource(), is(TEST_SOURCE));
    }

    @Test
    public void shouldGetEvent() throws Exception {
        Message uut = new Message();
        uut.setEvent(TEST_EVENT);

        assertThat(uut.getEvent(), is(TEST_EVENT));
    }

    @Test(expected = NullPointerException.class)
    public void shouldCatchInvalidEvent() throws Exception {
        Message uut = new Message();
        uut.setEvent(null);
    }

    @Test
    public void shouldBeIncomingIfSet() throws Exception {
        Message uut = new Message();
        uut.setIncoming(TEST_INCOMING);

        assertThat(uut.isIncoming(), is(TEST_INCOMING));
    }

    @Test
    public void shouldGetTargets() throws Exception {
        Message uut = new Message();
        uut.addTarget(TEST_TARGET);

        Set<InetSocketAddress> targets = uut.getTargets();

        assertThat(targets.size(), is(1));
        assertThat(targets.contains(TEST_TARGET), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void shouldCatchInvalidTargets() throws Exception {
        Message uut = new Message();
        uut.addTarget(null);
    }

    @Test
    public void shouldClearTargets() throws Exception {
        Message uut = new Message();
        uut.addTarget(TEST_TARGET);
        uut.clearTargets();

        Set<InetSocketAddress> targets = uut.getTargets();

        assertThat(targets.size(), is(0));
    }


}
