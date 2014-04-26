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
package com.zaradai.distributor.messaging;

import com.zaradai.mocks.MessageMocker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.BlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AbstractPendingCacheConnectionTest {
    private static final Message TEST_MESSAGE = MessageMocker.create();

    @Mock
    private BlockingQueue<Message> mockQueue;

    private boolean isConnected;
    private boolean doSendCalled;
    private int messagesSentViaDoSend;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doSendCalled = false;
        messagesSentViaDoSend = 0;
        isConnected = false;
    }

    @Test
    public void shouldCacheIfNotConnected() throws Exception {
        AbstractPendingCacheConnection uut = createdWithMockQueue();

        uut.send(TEST_MESSAGE);

        verify(mockQueue).put(TEST_MESSAGE);
    }

    @Test(expected = MessagingException.class)
    public void shouldThrowIfInterruptedWhilstAddingToQueue() throws Exception {
        AbstractPendingCacheConnection uut = createdWithMockQueue();
        doThrow(InterruptedException.class).when(mockQueue).put(TEST_MESSAGE);

        uut.send(TEST_MESSAGE);
    }

    @Test
    public void shouldNotCacheIfConnected() throws Exception {
        isConnected = true;
        AbstractPendingCacheConnection uut = createdWithMockQueue();

        uut.send(TEST_MESSAGE);

        verify(mockQueue, never()).put(TEST_MESSAGE);
        assertThat(doSendCalled, is(true));
    }

    @Test
    public void shouldFlushPending() throws Exception {
        AbstractPendingCacheConnection uut = create();
        uut.send(TEST_MESSAGE);
        isConnected = true;

        uut.flushPendingMessages();

        assertThat(messagesSentViaDoSend, is(1));
    }

    private AbstractPendingCacheConnection create() {
        return new AbstractPendingCacheConnection() {
            @Override
            protected void doSend(Message message) throws MessagingException {
                doSendCalled = true;
                messagesSentViaDoSend++;
            }

            @Override
            protected boolean isConnected() {
                return isConnected;
            }

            @Override
            public void shutdown() {

            }
        };
    }

    private AbstractPendingCacheConnection createdWithMockQueue() {
        return new AbstractPendingCacheConnection() {
            @Override
            protected BlockingQueue<Message> createPendingQueue() {
                return mockQueue;
            }

            @Override
            protected void doSend(Message message) throws MessagingException {
                doSendCalled = true;
                messagesSentViaDoSend++;
            }

            @Override
            protected boolean isConnected() {
                return isConnected;
            }

            @Override
            public void shutdown() {

            }
        };
    }
}
