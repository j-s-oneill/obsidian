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

import com.google.inject.Inject;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.netty.InetSocketAddressSerializer;
import com.zaradai.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.net.InetSocketAddress;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    private final Serializer serializer;

    @Inject
    MessageEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int index = out.writerIndex();
        // reserve space for length
        out.writeInt(0);
        // get the slot start for the message bytes
        int start = out.writerIndex();
        // write the message
        writeHeader(out);
        writeSource(msg, out);
        writeEvent(msg, out);
        // get slot end index;
        int end = out.writerIndex();
        // write out the bytes length
        out.setInt(index, (end - start));
    }

    private void writeHeader(ByteBuf out) throws EncodingException {
        try {
            out.writeInt(Message.MAGIC_NUMBER);
        } catch (Exception e) {
            throw new EncodingException("Unable to encode header", e);
        }
    }

    private void writeSource(Message msg, ByteBuf out) throws EncodingException {
        try {
            InetSocketAddressSerializer.serialize(msg.getSource(), out);
        } catch (Exception e) {
            throw new EncodingException("Unable to encode source", e);
        }
    }

    private void writeEvent(Message msg, ByteBuf out) throws EncodingException {
        try {
            ByteBufOutputStream outputStream = new ByteBufOutputStream(out);
            serializer.serialize(outputStream, msg.getEvent());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            throw new EncodingException("Unable to encode event", e);
        }
    }
}
