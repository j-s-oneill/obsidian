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

import com.google.inject.Inject;
import com.zaradai.config.ConfigurationSource;

public class DistributorConfigImpl implements DistributorConfig {
    private static final String PRE = "net";

    static final String PORT = PRE + ".port";
    static final String HOST = PRE + ".host";
    static final String RETRY_ATTEMPTS = PRE + ".retry.attempts";
    static final String RETRY_DELAY = PRE + ".retry.delay";
    static final String RETRY_MAX_DELAY = PRE + ".retry.max.delay";
    static final String RETRY_USE_EXPONENTIAL = PRE + ".retry.use.exp";
    static final String TCP_NO_DELAY = PRE + ".tcp.nodelay";
    static final String KEEP_ALIVE = PRE + ".keep.alive";
    static final String ACCEPT_BACKLOG = PRE + ".accept.backlog";
    static final String REUSE_ADDRESS = PRE + ".reuse.address";
    static final String CONNECTION_TIMEOUT = PRE + ".connection.timeout";

    static final int DEFAULT_PORT = 1907;
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_RETRY_ATTEMPTS = 5;
    static final long DEFAULT_RETRY_DELAY = 1000;
    static final long DEFAULT_RETRY_MAX_DELAY = 32000;
    static final boolean DEFAULT_RETRY_USE_EXPONENTIAL = true;
    static final boolean DEFAULT_TCP_NO_DELAY = true;
    static final boolean DEFAULT_KEEP_ALIVE = true;
    static final int DEFAULT_ACCEPT_BACKLOG = 100;
    static final boolean DEFAULT_REUSE_ADDRESS = true;
    static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    private final ConfigurationSource source;

    @Inject
    DistributorConfigImpl(ConfigurationSource source) {
        this.source = source;
    }

    @Override
    public int getPort() {
        return source.get(PORT, DEFAULT_PORT);
    }

    @Override
    public String getHost() {
        return source.get(HOST, DEFAULT_HOST);
    }

    @Override
    public int getRetryAttempts() {
        return source.get(RETRY_ATTEMPTS, DEFAULT_RETRY_ATTEMPTS);
    }

    @Override
    public long getRetryDelay() {
        return source.get(RETRY_DELAY, DEFAULT_RETRY_DELAY);
    }

    @Override
    public long getRetryMaxDelay() {
        return source.get(RETRY_MAX_DELAY, DEFAULT_RETRY_MAX_DELAY);
    }

    @Override
    public boolean getRetryUseExponentialBackOff() {
        return source.get(RETRY_USE_EXPONENTIAL, DEFAULT_RETRY_USE_EXPONENTIAL);
    }

    @Override
    public boolean getTcpNoDelay() {
        return source.get(TCP_NO_DELAY, DEFAULT_TCP_NO_DELAY);
    }

    @Override
    public boolean getKeepAlive() {
        return source.get(KEEP_ALIVE, DEFAULT_KEEP_ALIVE);
    }

    @Override
    public int getAcceptBacklog() {
        return source.get(ACCEPT_BACKLOG, DEFAULT_ACCEPT_BACKLOG);
    }

    @Override
    public boolean getReuseAddress() {
        return source.get(REUSE_ADDRESS, DEFAULT_REUSE_ADDRESS);
    }

    @Override
    public int getConnectionTimeout() {
        return source.get(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }
}
