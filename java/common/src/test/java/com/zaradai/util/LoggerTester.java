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
package com.zaradai.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.VerificationMode;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Helps to unit test logging messages using a logger that is embedded in code under test.
 * 1. Create an appender before the code being tested is executed.
 * 2. Once code has executed capture any messages logged to the appender just created.
 */
public class LoggerTester {
    public static Appender create() {
        final Appender appender = mock(Appender.class);
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        when(appender.getName()).thenReturn("MOCK");
        root.addAppender(appender);

        return appender;
    }

    public static List<String> captureLogMessages(Appender appender) {
        return captureLogMessages(appender, atLeastOnce());
    }

    public static List<String> captureLogMessages(Appender appender, int wantedInvocations) {
        return captureLogMessages(appender, times(wantedInvocations));
    }

    private static List<String> captureLogMessages(Appender appender, VerificationMode mode) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appender, mode).doAppend(captor.capture());

        return loggingEventsToStrings(captor);
    }

    private static List<String> loggingEventsToStrings(ArgumentCaptor<Object> captor) {
        List<String> res = Lists.newArrayList();

        for (Object value : captor.getAllValues()) {
            res.add(((LoggingEvent)value).getFormattedMessage());
        }

        return res;
    }
}
