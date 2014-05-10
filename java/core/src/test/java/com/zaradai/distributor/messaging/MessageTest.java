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
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class MessageTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);
    private static final Object TEST_EVENT = new Object();
    private static final boolean TEST_INCOMING = true;

    @Test
    public void shouldGetSource() throws Exception {
        Message uut = new Message();
        uut.setSource(TEST_ADDRESS);

        assertThat(uut.getSource(), is(TEST_ADDRESS));
    }

    @Test
    public void shouldGetEvent() throws Exception {
        Message uut = new Message();
        uut.setEvent(TEST_EVENT);

        assertThat(uut.getEvent(), is(TEST_EVENT));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfSettingWithNullEvent() throws Exception {
        Message uut = new Message();
        uut.setEvent(null);
    }

    @Test
    public void shouldGetIncoming() throws Exception {
        Message uut = new Message();
        uut.setIncoming(TEST_INCOMING);

        assertThat(uut.isIncoming(), is(TEST_INCOMING));
    }

    @Test
    public void shouldAddTarget() throws Exception {
        Message uut = new Message();
        uut.addTarget(TEST_ADDRESS);

        Set<InetSocketAddress> res = uut.getTargets();

        assertThat(res.size(), is(1));
        assertThat(res.contains(TEST_ADDRESS), is(true));
    }

    @Test
    public void shouldClearTargets() throws Exception {
        Message uut = new Message();
        uut.addTarget(TEST_ADDRESS);

        uut.clearTargets();

        Set<InetSocketAddress> res = uut.getTargets();

        assertThat(res.size(), is(0));
    }

    @Test
    public void shouldCreateWihBuilder() throws Exception {
        Message uut = new Message.Builder().addTarget(TEST_ADDRESS).event(TEST_EVENT).from(TEST_ADDRESS).build();

        assertThat(uut.getEvent(), is(TEST_EVENT));
        assertThat(uut.isIncoming(), is(false));
        assertThat(uut.getSource(), is(TEST_ADDRESS));

        Set<InetSocketAddress> res = uut.getTargets();

        assertThat(res.size(), is(1));
        assertThat(res.contains(TEST_ADDRESS), is(true));
    }
}
