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

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class AbstractPendingConnectionTest {
    private static final Message TEST_MESSAGE = MessageMocker.create();
    @Mock
    private BlockingQueue<Message> mockQueue;
    private boolean connectCalled;
    private boolean doSendCalled;
    private boolean isConnected;
    private boolean shutdownCalled;
    private AbstractPendingConnection uut;
    private Message sentMessage;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        connectCalled = false;
        doSendCalled =false;
        isConnected = false;
        shutdownCalled = false;
        sentMessage = null;

        uut = new AbstractPendingConnection() {
            @Override
            protected BlockingQueue<Message> createPendingQueue() {
                return mockQueue;
            }

            @Override
            protected void connect() {
                connectCalled = true;
            }

            @Override
            protected void doSend(Message message) throws MessagingException {
                doSendCalled = true;
                sentMessage = message;
            }

            @Override
            protected boolean isConnected() {
                return isConnected;
            }

            @Override
            public void shutdown() {
                shutdownCalled = true;
            }
        };
    }

    @Test
    public void shouldSendIfConnected() throws Exception {
        isConnected = true;
        uut.send(TEST_MESSAGE);

        assertThat(doSendCalled, is(true));
        assertThat(sentMessage, is(TEST_MESSAGE));
    }

    @Test
    public void shouldQueueMessageIfNotConnected() throws Exception {
        isConnected = false;
        uut.send(TEST_MESSAGE);

        verify(mockQueue).put(TEST_MESSAGE);
    }

    @Test
    public void shouldTryConnectIfQueueingMessage() throws Exception {
        isConnected = false;
        uut.send(TEST_MESSAGE);

        assertThat(connectCalled, is(true));
    }

    @Test(expected = MessagingException.class)
    public void shouldThrowIfInterrupted() throws Exception {
        doThrow(InterruptedException.class).when(mockQueue).put(TEST_MESSAGE);
        isConnected = false;
        uut.send(TEST_MESSAGE);
    }

    @Test
    public void shouldDrainMessages() throws Exception {
        uut = new AbstractPendingConnection() {
            @Override
            protected void connect() {
            }

            @Override
            protected void doSend(Message message) throws MessagingException {
            }

            @Override
            protected boolean isConnected() {
                return false;
            }

            @Override
            public void shutdown() {
            }
        };
        uut.send(TEST_MESSAGE);

        List<Message> pending = uut.drainPending();

        assertThat(pending.size(), is(1));
        assertThat(pending.get(0), is(TEST_MESSAGE));
    }
}
