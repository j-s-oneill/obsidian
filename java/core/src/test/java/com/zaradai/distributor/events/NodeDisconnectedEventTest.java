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

import org.junit.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class NodeDisconnectedEventTest {
    private static final InetSocketAddress TEST_ADDRESS = mock(InetSocketAddress.class);

    @Test
    public void shouldCreateWithAddress() throws Exception {
        NodeDisconnectedEvent uut = new NodeDisconnectedEvent(TEST_ADDRESS);

        assertThat(uut.getAddress(), is(TEST_ADDRESS));
    }

    @Test
    public void shouldGetAddress() throws Exception {
        NodeDisconnectedEvent uut = new NodeDisconnectedEvent();

        uut.setAddress(TEST_ADDRESS);

        assertThat(uut.getAddress(), is(TEST_ADDRESS));
    }
}
