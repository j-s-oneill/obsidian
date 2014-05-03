package com.zaradai.distributor;

import com.google.common.util.concurrent.Uninterruptibles;
import com.zaradai.distributor.events.SendMessageEvent;
import org.junit.Test;

import java.util.concurrent.*;

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

public class LocalhostTest {
    @Test
    public void shouldRunTest() throws Exception {
        ExecutorService taskExecutor = Executors.newFixedThreadPool(2);
        CompletionService<Void> completionService = new ExecutorCompletionService<Void>(taskExecutor);

        TestApp app1 = new TestApp(1703);
        TestApp app2 = new TestApp(1704);

        addTest(completionService,  app1);
        addTest(completionService,  app2);

        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        app1.postEvent(new SendMessageEvent(1704, new SendMessageEvent()));
        //app2.postEvent(new SendMessageEvent(1703, new SendMessageEvent()));
        //app2.postEvent(new SendMessageEvent(1704, new SendMessageEvent()));

        completionService.take().get();
        completionService.take().get();
    }

    private void addTest(CompletionService<Void> completionService, final TestApp app) {
        completionService.submit(new Runnable() {
            @Override
            public void run() {
                app.run();
            }
        }, null);
    }
}
