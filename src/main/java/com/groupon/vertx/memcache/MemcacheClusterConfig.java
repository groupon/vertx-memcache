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
package com.groupon.vertx.memcache;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.Logger;

/**
 * An object representing the required config for a Memcache cluster.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.1.0
 */
public class MemcacheClusterConfig implements MemcacheKeys {
    private static final Logger log = Logger.getLogger(MemcacheClusterConfig.class);

    private String eventBusAddressPrefix;
    private long retryInterval = MemcacheConfig.DEFAULT_RETRY_INTERVAL;
    private Map<String, MemcacheConfig> clusterMap = new HashMap<>();

    public MemcacheClusterConfig(JsonObject jsonConfig) {
        if (jsonConfig == null) {
            log.error("initialize", "exception", "noConfigFound");
            throw new MemcacheException("No Memcache cluster config found");
        }

        this.eventBusAddressPrefix = jsonConfig.getString(EVENT_BUS_ADDRESS_PREFIX_KEY);
        this.retryInterval = jsonConfig.getLong(RETRY_INTERVAL, MemcacheConfig.DEFAULT_RETRY_INTERVAL);
        JsonObject clusters = jsonConfig.getJsonObject(CLUSTERS_KEY, new JsonObject());

        if (eventBusAddressPrefix != null && !eventBusAddressPrefix.isEmpty() && clusters.size() > 0) {
            for (String clusterKey : clusters.fieldNames()) {
                JsonObject clusterConfig = clusters.getJsonObject(clusterKey, new JsonObject()).copy();
                clusterConfig.put(EVENT_BUS_ADDRESS_KEY, eventBusAddressPrefix);
                clusterConfig.put(RETRY_INTERVAL, retryInterval);
                clusterMap.put(clusterKey, new MemcacheConfig(clusterConfig));
            }
        } else {
            log.error("initialize", "exception", "invalidConfigFound", new String[] {"config"}, jsonConfig.encode());
            throw new MemcacheException("Invalid Memcache config defined");
        }

        log.info("initialize", "success", new String[]{"eventBusAddressPrefix", "clusters"}, eventBusAddressPrefix, clusterMap.size());
    }

    public String getEventBusAddressPrefix() {
        return eventBusAddressPrefix;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public Set<String> getClusterNames() {
        return Collections.unmodifiableSet(clusterMap.keySet());
    }

    public MemcacheConfig getCluster(String clusterName) {
        return clusterMap.get(clusterName);
    }

    public Set<String> getServers() {
        Set<String> servers = new HashSet<>();
        for (MemcacheConfig config : clusterMap.values()) {
            servers.addAll(config.getServers());
        }
        return Collections.unmodifiableSet(servers);
    }
}
