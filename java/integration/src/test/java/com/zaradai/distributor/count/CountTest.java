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
package com.zaradai.distributor.count;

import com.google.common.util.concurrent.Uninterruptibles;
import com.zaradai.distributor.TestEvent;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class CountTest {
    @Test
    public void shouldRun() throws Exception {
        SimpleDistributorService dis1 = new SimpleDistributorService();
        SimpleDistributorService dis2 = new SimpleDistributorService();
        SimpleDistributorService dis3 = new SimpleDistributorService();
        SimpleDistributorService dis4 = new SimpleDistributorService();
        // setup
        dis1.setPort(1710);
        dis2.setPort(1711);
        dis3.setPort(1712);
        dis4.setPort(1713);
        // start up
        dis1.start();
        dis2.start();
        dis3.start();
        dis4.start();
         // process messages
        InetAddress local = InetAddress.getByName(dis1.getHost());

        dis1.post(new TestEvent(),
                new InetSocketAddress(local, 1711),
                new InetSocketAddress(local, 1712),
                new InetSocketAddress(local, 1713));

        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
        // shutdown
        dis1.stop();
        dis2.stop();
        dis3.stop();
        dis4.stop();
    }
}
