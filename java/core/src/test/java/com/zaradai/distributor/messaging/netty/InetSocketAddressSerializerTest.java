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
package com.zaradai.distributor.messaging.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InetSocketAddressSerializerTest {
    private static final int TEST_PORT = 80;

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        InetSocketAddress test = new InetSocketAddress(TEST_PORT);
        ByteBuf buffer = Unpooled.buffer(64);
        // serialize
        InetSocketAddressSerializer.serialize(test, buffer);
        // now deserialize
        InetSocketAddress res = InetSocketAddressSerializer.deserialize(buffer);

        assertThat(res, is(test));
    }
}
