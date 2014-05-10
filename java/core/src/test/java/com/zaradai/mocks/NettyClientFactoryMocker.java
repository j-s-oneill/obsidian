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
package com.zaradai.mocks;

import com.zaradai.distributor.messaging.netty.NettyClient;
import com.zaradai.distributor.messaging.netty.NettyClientFactory;

import java.net.InetSocketAddress;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NettyClientFactoryMocker {
    public static NettyClientFactory create() {
        return mock(NettyClientFactory.class);
    }

    public static NettyClientFactory create(NettyClient client) {
        NettyClientFactory res = create();
        when(res.create(any(InetSocketAddress.class))).thenReturn(client);

        return res;
    }

    public static NettyClientFactory create(NettyClient client, InetSocketAddress address) {
        NettyClientFactory res = create();
        when(res.create(address)).thenReturn(client);

        return res;
    }
}
