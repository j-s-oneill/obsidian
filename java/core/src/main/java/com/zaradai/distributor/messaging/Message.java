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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;

public class Message {
    public static final int MAGIC_NUMBER = 0xFA4527D8;

    private UUID id;
    private Set<InetSocketAddress> targets;
    private InetSocketAddress source;
    private Object event;
    private boolean incoming;

    public Message() {
        id = UUID.randomUUID();
        targets = createTargetSet();
    }

    public Message(Object event) {
        this();
        setEvent(event);
    }

    protected Set<InetSocketAddress> createTargetSet() {
        return Sets.newHashSet();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public void setSource(InetSocketAddress source) {
        this.source = source;
    }

    public Object getEvent() {
        return event;
    }

    public void setEvent(Object event) {
        this.event = Preconditions.checkNotNull(event, "Invalid event");
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public Set<InetSocketAddress> getTargets() {
        return ImmutableSet.copyOf(targets);
    }

    public void addTarget(InetSocketAddress address) {
        Preconditions.checkNotNull(address, "Invalid target address");
        targets.add(address);
    }

    public void clearTargets() {
        targets.clear();
    }
}
