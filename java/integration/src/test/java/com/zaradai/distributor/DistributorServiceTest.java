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

import com.google.common.collect.Lists;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DistributorServiceTest {
    @Test
    public void shouldPingPong() throws Exception {
        boolean verboseLogging = false;
        // Create the set of address we shall use in the test
        InetAddress local = createLocalAddress();
        InetSocketAddress source1 = new InetSocketAddress(local, 1710);
        InetSocketAddress source2 = new InetSocketAddress(local, 1711);
        InetSocketAddress source3 = new InetSocketAddress(local, 1712);
        InetSocketAddress source4 = new InetSocketAddress(local, 1713);

        SimpleDistributorService dis1 = new SimpleDistributorService();
        SimpleDistributorService dis2 = new SimpleDistributorService();
        SimpleDistributorService dis3 = new SimpleDistributorService();
        SimpleDistributorService dis4 = new SimpleDistributorService();
        // setup
        dis1.setPort(source1.getPort());
        dis1.setHost(source1.getHostName());
        dis1.setVerboseLogging(verboseLogging);
        dis2.setPort(source2.getPort());
        dis2.setHost(source2.getHostName());
        dis2.setVerboseLogging(verboseLogging);
        dis3.setPort(source3.getPort());
        dis3.setHost(source3.getHostName());
        dis3.setVerboseLogging(verboseLogging);
        dis4.setPort(source4.getPort());
        dis4.setHost(source4.getHostName());
        dis4.setVerboseLogging(verboseLogging);
        // get the testers
        // NOTE: although we don't need to get testers 2,3,4 to call on but by doing so we ensure their ctors are
        // called and so messages are subscribed to, in a production system this would be taken care of through
        // natural creation of the application state.
        PingPongTester tester1 = dis1.getTester();
        PingPongTester tester2 = dis2.getTester();
        PingPongTester tester3 = dis3.getTester();
        PingPongTester tester4 = dis4.getTester();
        // start up
        dis1.start();
        dis2.start();
        dis3.start();
        dis4.start();
        // start ping pong from 1
        tester1.test(Lists.newArrayList(source2, source3, source4));
        // wait for it to finish
        boolean res = tester1.waitUntilFinishes(10, TimeUnit.SECONDS);
        // shutdown
        dis1.stop();
        dis2.stop();
        dis3.stop();
        dis4.stop();

        assertThat(res, is(true));
    }

    private InetAddress createLocalAddress() {
        InetAddress res;

        try {
            res = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            res = InetAddress.getLoopbackAddress();
        }

        return res;
    }
}
