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
import com.zaradai.net.authentication.ConnectionAuthenticator;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ConnectionAuthenticatorHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionAuthenticatorHandler.class);

    private final ConnectionAuthenticator authenticator;

    @Inject
    ConnectionAuthenticatorHandler(ConnectionAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        // no more needed
        ctx.pipeline().remove(this);

        if (address != null) {
            if (authenticator.authenticate(address)) {
                accepted(ctx, address);
            } else {
                rejected(ctx, address);
            }
        } else {
            // No remote address so this is a local connection, assume authenticated??
            LOGGER.debug("Remote address is invalid, unable to authenticate");
        }

        super.channelActive(ctx);
    }

    private void rejected(ChannelHandlerContext ctx, SocketAddress address) {
        LOGGER.warn("Remote address {} rejected not authenticated, closing connection", address);
        ctx.close();

    }

    private void accepted(ChannelHandlerContext ctx, SocketAddress address) {
        LOGGER.debug("Remote address {} accepted authenticated", address);
    }
}
