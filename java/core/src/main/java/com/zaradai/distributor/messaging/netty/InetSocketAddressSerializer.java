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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class InetSocketAddressSerializer {
    public static void serialize(InetSocketAddress address, ByteBuf out) {
        byte[] bytes = address.getAddress().getAddress();
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        out.writeInt(address.getPort());
    }

    public static InetSocketAddress deserialize(ByteBuf in) throws UnknownHostException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        InetAddress inetAddress = InetAddress.getByAddress(bytes);
        // read port and return
        return new InetSocketAddress(inetAddress, in.readInt());
    }
}
