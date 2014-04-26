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

import com.zaradai.distributor.messaging.Client;
import com.zaradai.distributor.messaging.ClientFactory;

import java.net.InetSocketAddress;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientFactoryMocker {
    public static ClientFactory create() {
        return mock(ClientFactory.class);
    }

    public static ClientFactory create(Client client) {
        ClientFactory res = create();
        when(res.create(any(InetSocketAddress.class))).thenReturn(client);

        return res;
    }

    public static ClientFactory create(Client client, InetSocketAddress address) {
        ClientFactory res = create();
        when(res.create(address)).thenReturn(client);

        return res;
    }
}
