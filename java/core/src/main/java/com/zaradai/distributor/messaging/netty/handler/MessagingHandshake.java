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
import com.google.inject.assistedinject.Assisted;
import com.zaradai.distributor.config.DistributorConfig;
import com.zaradai.distributor.messaging.ConnectionManager;
import com.zaradai.distributor.messaging.netty.ChannelConnection;
import com.zaradai.distributor.messaging.netty.InetSocketAddressSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class MessagingHandshake extends AbstractHandshakeHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingHandshake.class);
    private static final int PROTOCOL_HEADER = 0xFA45A99;
    private final DistributorConfig config;
    private final ConnectionManager connectionManager;
    private int serverHeaderSize = -1;

    @Inject
    MessagingHandshake(DistributorConfig config, ConnectionManager connectionManager,
                              @Assisted Boolean isClient) {
        super(isClient, config.getHandshakeTimeout());
        this.config = config;
        this.connectionManager = connectionManager;
    }

    @Override
    protected boolean canRead(boolean client, ByteBuf in) {
        if (client) {
            return canReadClientHeader(in);
        } else {
            return canReadServerHeader(in);
        }
    }

    @Override
    protected void clientWrite(ByteBuf out) {
        int idx = out.writerIndex();
        // reserve space for length
        out.writeInt(0);
        // get slot for the bytes
        int start = out.writerIndex();
        // write protocol header
        writeProtocolHeader(out);
        writeAddress(out);
        // note end of the object slot
        int end = out.writerIndex();
        // write length
        out.setInt(idx, (end - start));
    }

    @Override
    protected void serverWrite(ByteBuf out) {
        writeProtocolHeader(out);
    }

    @Override
    protected boolean serverReadAndValidate(ChannelHandlerContext ctx, ByteBuf in) {
        int protocol = readProtocolHeader(in);

        if (protocol == PROTOCOL_HEADER) {
            try {
                InetSocketAddress address = readAddress(in);
                // activate the caller
                activateConnection(address, ctx.channel());

                return true;
            } catch (UnknownHostException e) {
                LOGGER.debug("Unable to read address from client", e);
            }
        }

        return false;
    }


    @Override
    protected boolean clientReadAndValidate(ChannelHandlerContext ctx, ByteBuf in) {
        if (readProtocolHeader(in) == PROTOCOL_HEADER) {
            activateConnection((InetSocketAddress) ctx.channel().remoteAddress(), ctx.channel());
            return true;
        }

        return false;
    }

    private void activateConnection(InetSocketAddress socketAddress, Channel channel) {
        // get or create associated channel
        ChannelConnection connection = (ChannelConnection) connectionManager.getOrCreate(socketAddress);
        // now activate
        connection.setChannel(channel);
    }


    private boolean canReadServerHeader(ByteBuf in) {
        if (serverHeaderSize == -1) {
            if (in.readableBytes() >= 4) {
                serverHeaderSize = in.readInt();
            } else {
                return false;
            }
        }

        return (in.readableBytes() >= serverHeaderSize);
    }

    private boolean canReadClientHeader(ByteBuf in) {
        return in.readableBytes() >= 4;
    }

    private void writeProtocolHeader(ByteBuf out) {
        out.writeInt(PROTOCOL_HEADER);
    }

    private int readProtocolHeader(ByteBuf in) {
        return in.readInt();
    }

    private InetSocketAddress readAddress(ByteBuf in) throws UnknownHostException {
        return InetSocketAddressSerializer.deserialize(in);
    }

    private void writeAddress(ByteBuf out) {
        InetSocketAddress address;

        try {
            InetAddress local = InetAddress.getByName(config.getHost());
            address = new InetSocketAddress(local, config.getPort());
        } catch (UnknownHostException e) {
            LOGGER.debug("Unable to get host address", e);
            address = new InetSocketAddress(config.getPort());
        }

        InetSocketAddressSerializer.serialize(address, out);
    }
}
