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
package com.zaradai.net.retry;

import ch.qos.logback.core.Appender;
import com.zaradai.util.LoggerTester;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NoDelayTest {
    @Test
    public void shouldNotDelayButLog() throws Exception {
        Appender appender = LoggerTester.create();
        NoDelay uut = new NoDelay();

        uut.delay();

        List<String> logs = LoggerTester.captureLogMessages(appender);

        assertThat(logs.size(), is(1));
        assertThat(logs.get(0), containsString("Retrying"));
    }
}
