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

import com.zaradai.distributor.messaging.Message;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageMocker {
    public static Message create() {
        return mock(Message.class);
    }

    public static Message create(boolean incoming) {
        Message res = create();
        when(res.isIncoming()).thenReturn(incoming);

        return res;
    }

    public static Message create(boolean incoming, Object event) {
        Message res = create(incoming);
        when(res.getEvent()).thenReturn(event);

        return res;
    }

    public static Message create(boolean incoming, Object event, Set<InetSocketAddress> targets) {
        Message res = create(incoming, event);
        when(res.getTargets()).thenReturn(targets);

        return res;
    }
}
