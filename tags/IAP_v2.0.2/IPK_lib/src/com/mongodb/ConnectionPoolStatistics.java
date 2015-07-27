/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
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

package com.mongodb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An MBean implementation for connection pool statistics.
 */
final class ConnectionPoolStatistics extends ConnectionPoolListenerAdapter implements ConnectionPoolStatisticsMBean {
    private final ServerAddress serverAddress;
    private final ConnectionPoolSettings settings;
    private final AtomicInteger size = new AtomicInteger();
    private final AtomicInteger checkedOutCount = new AtomicInteger();
    private final AtomicInteger waitQueueSize = new AtomicInteger();

    public ConnectionPoolStatistics(final ConnectionPoolOpenedEvent event) {
        serverAddress = event.getServerAddress();
        settings = event.getSettings();
    }

    @Override
    public String getHost() {
        return serverAddress.getHost();
    }

    @Override
    public int getPort() {
        return serverAddress.getPort();
    }

    @Override
    public int getMinSize() {
        return settings.getMinSize();
    }

    @Override
    public int getMaxSize() {
        return settings.getMaxSize();
    }

    @Override
    public int getSize() {
        return size.get();
    }

    @Override
    public int getCheckedOutCount() {
        return checkedOutCount.get();
    }

    @Override
    public int getWaitQueueSize() {
        return waitQueueSize.get();
    }

    @Override
    public void connectionCheckedOut(final ConnectionEvent event) {
        checkedOutCount.incrementAndGet();
    }

    @Override
    public void connectionCheckedIn(final ConnectionEvent event) {
        checkedOutCount.decrementAndGet();
    }

    @Override
    public void connectionAdded(final ConnectionEvent event) {
        size.incrementAndGet();
    }

    @Override
    public void connectionRemoved(final ConnectionEvent event) {
        size.decrementAndGet();
    }

    @Override
    public void waitQueueEntered(final ConnectionPoolWaitQueueEvent event) {
        waitQueueSize.incrementAndGet();
    }

    @Override
    public void waitQueueExited(final ConnectionPoolWaitQueueEvent event) {
        waitQueueSize.decrementAndGet();
    }
}