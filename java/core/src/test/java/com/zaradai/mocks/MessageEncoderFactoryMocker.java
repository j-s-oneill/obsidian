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

import com.zaradai.distributor.messaging.netty.handler.MessageEncoder;
import com.zaradai.distributor.messaging.netty.handler.MessageEncoderFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageEncoderFactoryMocker {
    public static MessageEncoderFactory create() {
        return mock(MessageEncoderFactory.class);
    }

    public static MessageEncoderFactory create(MessageEncoder messageEncoder) {
        MessageEncoderFactory res = create();
        when(res.create()).thenReturn(messageEncoder);

        return res;
    }

}
