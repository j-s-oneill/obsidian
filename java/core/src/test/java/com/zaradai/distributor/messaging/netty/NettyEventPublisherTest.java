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

import com.zaradai.events.EventAggregator;
import com.zaradai.mocks.EventAggregatorMocker;
import com.zaradai.mocks.EventLoopGroupsMocker;
import io.netty.channel.EventLoopGroup;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class NettyEventPublisherTest {


    private static final Object TEST_EVENT = new Object();

    @Test
    public void shouldSubmitOnExecutor() throws Exception {
        EventAggregator eventAggregator = EventAggregatorMocker.create();
        EventLoopGroups eventLoopGroups = EventLoopGroupsMocker.create();
        final ExecutorService executorService = mock(ExecutorService.class);

        NettyEventPublisher uut = new NettyEventPublisher(eventAggregator, eventLoopGroups) {
            @Override
            ExecutorService getExecutor() {
                return executorService;
            }
        };

        uut.publish(TEST_EVENT);

        verify(executorService).submit(any(Runnable.class));
    }

    @Test
    public void shouldSubmitOnClientGroup() throws Exception {
        EventAggregator eventAggregator = EventAggregatorMocker.create();
        EventLoopGroups eventLoopGroups = EventLoopGroupsMocker.create();
        when(eventLoopGroups.getClientGroup()).thenReturn(mock(EventLoopGroup.class));

        NettyEventPublisher uut = new NettyEventPublisher(eventAggregator, eventLoopGroups);

        uut.publish(TEST_EVENT);

        verify(eventLoopGroups).getClientGroup();
    }
}
