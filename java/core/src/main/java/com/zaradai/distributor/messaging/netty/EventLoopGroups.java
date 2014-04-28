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
package com.zaradai.distributor.messaging.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class EventLoopGroups {
    private final EventLoopGroup serverGroup;
    private final EventLoopGroup clientGroup;

    public EventLoopGroups() {
        clientGroup = createClientGroup();
        serverGroup = createServerGroup();
    }

    protected EventLoopGroup createServerGroup() {
        return new NioEventLoopGroup(1);
    }

    protected EventLoopGroup createClientGroup() {
        return new NioEventLoopGroup();
    }

    public EventLoopGroup getClientGroup() {
        return clientGroup;
    }

    public EventLoopGroup getServerGroup() {
        return serverGroup;
    }

    public void shutdown() {
        serverGroup.shutdownGracefully();
        clientGroup.shutdownGracefully();
    }
}
