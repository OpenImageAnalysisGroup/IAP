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

import java.util.concurrent.TimeUnit;

/**
 * Represents a cluster of MongoDB servers.  Implementations can define the behaviour depending upon the type of cluster.
 */
interface Cluster {

    /**
     * Get the description of this cluster.  This method will not return normally until the cluster type is known.
     *
     * @param maxWaitTime the maximum time to wait for a connection to the cluster to get the description
     * @param timeUnit    the TimeUnit for the maxWaitTime
     * @return a ClusterDescription representing the current state of the cluster
     * @throws com.mongodb.MongoTimeoutException if the timeout has been reached before the cluster type is known
     */
    ClusterDescription getDescription(long maxWaitTime, TimeUnit timeUnit);

    /**
     * Get a MongoDB server that matches the criteria defined by the serverSelector
     *
     * @param serverSelector a ServerSelector that defines how to select the required Server
     * @param maxWaitTime    the maximum time to wait for a connection to the cluster to get a server
     * @param timeUnit       the TimeUnit for the maxWaitTime
     * @return a Server that meets the requirements
     * @throws com.mongodb.MongoTimeoutException if the timeout has been reached before a server matching the selector is available
     */
    Server getServer(ServerSelector serverSelector, long maxWaitTime, TimeUnit timeUnit);

    /**
     * Closes connections to the servers in the cluster.  After this is called, this cluster instance can no longer be used.
     */
    void close();

    /**
     * Whether all the servers in the cluster are closed or not.
     *
     * @return true if all the servers in this cluster have been closed
     */
    boolean isClosed();
}
