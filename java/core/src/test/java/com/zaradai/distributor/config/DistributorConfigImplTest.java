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
package com.zaradai.distributor.config;

import com.zaradai.config.ConfigurationSource;
import com.zaradai.mocks.ConfigurationSourceMocker;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;

public class DistributorConfigImplTest {
    private ConfigurationSource source;
    private DistributorConfigImpl uut;

    @Before
    public void setUp() throws Exception {
        source = ConfigurationSourceMocker.create();
        uut = new DistributorConfigImpl(source);
    }

    @Test
    public void shouldGetPort() throws Exception {
        uut.getPort();

        verify(source).get(DistributorConfigImpl.PORT, DistributorConfigImpl.DEFAULT_PORT);
    }

    @Test
    public void shouldGetHost() throws Exception {
        uut.getHost();

        verify(source).get(DistributorConfigImpl.HOST, DistributorConfigImpl.DEFAULT_HOST);
    }

    @Test
    public void shouldGetRetryAttempts() throws Exception {
        uut.getRetryAttempts();

        verify(source).get(DistributorConfigImpl.RETRY_ATTEMPTS, DistributorConfigImpl.DEFAULT_RETRY_ATTEMPTS);
    }

    @Test
    public void shouldGetRetryDelay() throws Exception {
        uut.getRetryDelay();

        verify(source).get(DistributorConfigImpl.RETRY_DELAY, DistributorConfigImpl.DEFAULT_RETRY_DELAY);
    }

    @Test
    public void shouldGetRetryMaxDelay() throws Exception {
        uut.getRetryMaxDelay();

        verify(source).get(DistributorConfigImpl.RETRY_MAX_DELAY, DistributorConfigImpl.DEFAULT_RETRY_MAX_DELAY);
    }

    @Test
    public void shouldGetRetryExponentialBackOff() throws Exception {
        uut.getRetryUseExponentialBackOff();

        verify(source).get(DistributorConfigImpl.RETRY_USE_EXPONENTIAL, DistributorConfigImpl.DEFAULT_RETRY_USE_EXPONENTIAL);
    }

    @Test
    public void shouldGetTcpNoDelay() throws Exception {
        uut.getTcpNoDelay();

        verify(source).get(DistributorConfigImpl.TCP_NO_DELAY, DistributorConfigImpl.DEFAULT_TCP_NO_DELAY);
    }

    @Test
    public void shouldGetKeepAlive() throws Exception {
        uut.getKeepAlive();

        verify(source).get(DistributorConfigImpl.KEEP_ALIVE, DistributorConfigImpl.DEFAULT_KEEP_ALIVE);
    }

    @Test
    public void shouldGetAcceptBacklog() throws Exception {
        uut.getAcceptBacklog();

        verify(source).get(DistributorConfigImpl.ACCEPT_BACKLOG, DistributorConfigImpl.DEFAULT_ACCEPT_BACKLOG);

    }

    @Test
    public void shouldGetReuseAddress() throws Exception {
        uut.getReuseAddress();

        verify(source).get(DistributorConfigImpl.REUSE_ADDRESS, DistributorConfigImpl.DEFAULT_REUSE_ADDRESS);
    }
}
