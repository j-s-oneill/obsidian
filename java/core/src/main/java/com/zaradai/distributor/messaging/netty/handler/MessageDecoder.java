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
import com.zaradai.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

public class MessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final int MAX_MESSAGE_SIZE = 1024 * 1024;
    private static final int LENGTH_SIZE = 4;
    private final Serializer serializer;

    @Inject
    MessageDecoder(Serializer serializer) {
        super(MAX_MESSAGE_SIZE, 0, LENGTH_SIZE, 0, LENGTH_SIZE);
        this.serializer = serializer;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);

        if (frame == null) {
            return null;
        }

        Message res = new Message();

        readHeader(frame);
        readUuid(res, frame);
        readSource(res, frame);
        readEvent(res, frame);
        res.setIncoming(true);

        return res;
    }

    private void readHeader(ByteBuf frame) throws EncodingException {
        int header;

        try {
            header = frame.readInt();
        } catch (Exception e) {
            throw new EncodingException("Unable to read header data", e);
        }

        if (header != Message.MAGIC_NUMBER) {
            throw new EncodingException("Invalid header");
        }
    }

    private void readUuid(Message res, ByteBuf frame) throws EncodingException {
        try {
            UUID uuid = new UUID(frame.readLong(), frame.readLong());
            res.setId(uuid);
        } catch (Exception e) {
            throw new EncodingException("Unable to read ID", e);
        }
    }

    private void readSource(Message res, ByteBuf frame) throws EncodingException {
        try {
            int len = frame.readInt();
            byte[] bytes = new byte[len];
            frame.readBytes(bytes);
            int port = frame.readInt();

            res.setSource(new InetSocketAddress(InetAddress.getByAddress(bytes), port));
        } catch (Exception e) {
            throw new EncodingException("Unable to read source", e);
        }
    }

    private void readEvent(Message res, ByteBuf frame) throws EncodingException {
        try {
            ByteBufInputStream in = new ByteBufInputStream(frame);
            res.setEvent(serializer.deserialize(in));
        } catch (Exception e) {
            throw new EncodingException("Unable to read event", e);
        }
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }
}
