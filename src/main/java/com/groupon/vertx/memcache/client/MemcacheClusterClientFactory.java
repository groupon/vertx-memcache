/**
 * Copyright 2014 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.memcache.client;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.eventbus.EventBus;

import com.groupon.vertx.memcache.MemcacheClusterConfig;

/**
 * Factory for retrieving a cluster Memcache client.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.1.0
 */
public final class MemcacheClusterClientFactory {
    private Map<String, MemcacheClient> clientMap = new HashMap<>();

    public MemcacheClusterClientFactory(EventBus eventBus, MemcacheClusterConfig config) throws MemcacheClientException {
        if (eventBus == null || config == null) {
            throw new MemcacheClientException("Illegal arguments");
        }

        for (String clusterKey : config.getClusterNames()) {
            clientMap.put(clusterKey, new MemcacheClient(eventBus, config.getCluster(clusterKey)));
        }
    }

    public MemcacheClient getClient(String clusterName) {
        return clientMap.get(clusterName);
    }
}
