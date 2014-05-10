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
package com.zaradai.distributor.messaging.netty.handler;

import com.esotericsoftware.kryo.Kryo;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.serialization.Serializer;
import com.zaradai.serialization.kryo.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class MessageDecoderTest {
    private static final int TEST_VALUE = 675;
    private static final TestEvent TEST_EVENT = new TestEvent(TEST_VALUE);
    private final static InetSocketAddress TEST_ADDRESS = new InetSocketAddress("127.0.0.1", 80);

    private Message testMessage;

    @Before
    public void setUp() throws Exception {
        testMessage = new Message();
        testMessage.setEvent(TEST_EVENT);
        testMessage.setSource(TEST_ADDRESS);
    }

    @Test
    public void shouldDecode() throws Exception {
        Serializer serializer = new KryoSerializer(new Kryo());
        MessageEncoder encoder = new MessageEncoder(serializer);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        MessageDecoder uut = new MessageDecoder(serializer);
        ByteBuf buffer = createValidBuffer();
        encoder.encode(ctx, testMessage, buffer);

        Message res = (Message) uut.decode(ctx, buffer);

        assertThat(res.getSource(), is(TEST_ADDRESS));
        assertThat(((TestEvent) res.getEvent()).getTest(), is(TEST_VALUE));
    }

    ByteBuf createValidBuffer() {
        return Unpooled.buffer(1024);
    }
}
