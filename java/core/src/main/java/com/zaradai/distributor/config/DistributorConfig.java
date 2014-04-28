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

public interface DistributorConfig {
    /**
     * Return the port the distributor service listens on.
     * @return listening port.
     */
    int getPort();

    /**
     * Returns the host the distributor service listens on.
     * @return listening host name.
     */
    String getHost();

    // Retry logic

    /**
     * The number of times retry logic will attempt to connect to messaging target address.
     * If the value is -1, the retry logic will keep trying until the service is shutdown.
     * @return number of retry attempts
     */
    int getRetryAttempts();

    /**
     * Gets the time in milliseconds to pause between retry attempts.
     * @return retry delay in milliseconds
     */
    long getRetryDelay();

    /**
     * Get the maximum time in milliseconds between retry attempts
     * @return maximum delay in milliseconds.
     */
    long getRetryMaxDelay();

    /**
     * Determines is exponential back-off logic is used.
     * @return true if logic should use exponential back-off.
     */
    boolean getRetryUseExponentialBackOff();


    // Common TCP options to optimize connections
    boolean getTcpNoDelay();
    boolean getKeepAlive();
    int getAcceptBacklog();
    boolean getReuseAddress();
    int getConnectionTimeout();
}
