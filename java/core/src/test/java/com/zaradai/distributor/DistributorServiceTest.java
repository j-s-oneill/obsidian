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
package com.zaradai.distributor;

import com.google.common.collect.Sets;
import com.zaradai.distributor.messaging.Message;
import com.zaradai.distributor.messaging.MessagingService;
import com.zaradai.mocks.EventAggregatorMocker;
import com.zaradai.mocks.MessagingServiceMocker;
import com.zaradai.events.EventAggregator;
import com.zaradai.mocks.MessageMocker;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DistributorServiceTest {
    private static final Object TEST_EVENT = new Object();
    private static final InetSocketAddress TEST_TARGET_1 = mock(InetSocketAddress.class);
    private static final InetSocketAddress TEST_TARGET_2 = mock(InetSocketAddress.class);

    private EventAggregator eventAggregator;
    private MessagingService messagingService;
    private DistributorService uut;

    @Before
    public void setUp() throws Exception {
        eventAggregator = EventAggregatorMocker.create();
        messagingService = MessagingServiceMocker.create();
        uut = new DistributorService(eventAggregator, messagingService);
    }

    @Test
    public void shouldRegisterWithEventAggregatorOnCreate() throws Exception {
        verify(eventAggregator).subscribe(uut);
    }

    @Test
    public void shouldPublishIncomingMessage() throws Exception {
        Message message = MessageMocker.create(true, TEST_EVENT);

        uut.onMessage(message);

        verify(eventAggregator).publish(TEST_EVENT);
    }

    @Test
    public void shouldBroadcastIfOutgoingAndNoTargets() throws Exception {
        Message message = MessageMocker.create(false, TEST_EVENT);

        uut.onMessage(message);

        verify(messagingService).publish(message);
    }

    @Test
    public void shouldSendToTargetAddressIfOutgoing() throws Exception {
        Message message = MessageMocker.create(false, TEST_EVENT, Sets.newHashSet(TEST_TARGET_1, TEST_TARGET_2));

        uut.onMessage(message);

        verify(messagingService).send(TEST_TARGET_1, message);
        verify(messagingService).send(TEST_TARGET_2, message);
    }

    @Test
    public void shouldClearTargetsOnSending() throws Exception {
        Message message = MessageMocker.create(false, TEST_EVENT, Sets.newHashSet(TEST_TARGET_1, TEST_TARGET_2));

        uut.onMessage(message);

        verify(message).clearTargets();
    }

    @Test
    public void shouldStartUpMessagingServiceWhenStarted() throws Exception {
        uut.startAsync().awaitRunning();

        verify(messagingService, atLeastOnce()).startAsync();
    }

    @Test
    public void shouldShutdownMessagingServiceWhenStopped() throws Exception {
        uut.startAsync().awaitRunning();
        uut.stopAsync().awaitTerminated();

        verify(messagingService, atLeastOnce()).startAsync();
    }

}
