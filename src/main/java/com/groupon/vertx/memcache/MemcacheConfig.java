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

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.memcache.hash.HashAlgorithm;
import com.groupon.vertx.memcache.server.ContinuumType;
import com.groupon.vertx.utils.Logger;

/**
 * An object representing the required config for Memcache.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheConfig implements MemcacheKeys {
    private static final Logger log = Logger.getLogger(MemcacheConfig.class);
    protected static final int DEFAULT_POINTS_PER_SERVER = 160;
    protected static final long DEFAULT_RETRY_INTERVAL = 50;
    protected static final String VALID_SERVER = "[a-zA-Z0-9-.]+(:\\d+){0,2}$";

    private LinkedList<String> servers = new LinkedList<>();
    private String eventBusAddress;
    private String namespace;
    private ContinuumType continuum;
    private HashAlgorithm algorithm;
    private int pointsPerServer = DEFAULT_POINTS_PER_SERVER;
    private long retryInterval = DEFAULT_RETRY_INTERVAL;

    public MemcacheConfig(JsonObject jsonConfig) {
        if (jsonConfig == null) {
            log.error("initialize", "exception", "noConfigFound");
            throw new MemcacheException("No Memcache config found");
        }

        if (jsonConfig.getJsonArray(SERVERS_KEY) != null && jsonConfig.getString(EVENT_BUS_ADDRESS_KEY) != null &&
                !jsonConfig.getString(EVENT_BUS_ADDRESS_KEY).isEmpty()) {
            this.servers.addAll(processServers(jsonConfig.getJsonArray(SERVERS_KEY)));
            this.eventBusAddress = jsonConfig.getString(EVENT_BUS_ADDRESS_KEY);
            this.namespace = jsonConfig.getString(NAMESPACE_KEY);
            this.pointsPerServer = jsonConfig.getInteger(POINTS_PER_SERVER, DEFAULT_POINTS_PER_SERVER);
            this.retryInterval = jsonConfig.getLong(RETRY_INTERVAL, DEFAULT_RETRY_INTERVAL);

            final HashAlgorithm defaultHashAlgorithm = HashAlgorithm.FNV1_32_HASH;
            String algorithmStr = jsonConfig.getString(ALGORITHM_KEY, defaultHashAlgorithm.name());
            this.algorithm = algorithmStr == null ? defaultHashAlgorithm : HashAlgorithm.valueOf(algorithmStr);

            final ContinuumType defaultContinuumType = ContinuumType.KETAMA;
            String continuumStr = jsonConfig.getString(CONTINUUM_KEY, defaultContinuumType.name());
            this.continuum = continuumStr == null ? defaultContinuumType : ContinuumType.valueOf(continuumStr);
        } else {
            log.error("initialize", "exception", "invalidConfigFound", new String[] {"config"}, jsonConfig.encode());
            throw new MemcacheException("Invalid Memcache config defined");
        }

        log.info("initialize", "success", new String[]{"eventBusAddress", "namespace", "servers", "algorithm"}, eventBusAddress,
                namespace, servers.size(), algorithm);
    }

    public Collection<String> getServers() {
        return servers;
    }

    public String getEventBusAddress() {
        return eventBusAddress;
    }

    public String getNamespace() {
        return namespace;
    }

    public HashAlgorithm getHashAlgorithm() {
        return algorithm;
    }

    public ContinuumType getContinuumType() {
        return continuum;
    }

    public int getPointsPerServer() {
        return pointsPerServer;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    /**
     * Process a JsonArray of server strings and return a collection of the valid entries.  Valid server strings should
     * be in the format [hostname]:[port].
     *
     * @param serverStrings
     * @return A Collection of valid servers.
     */
    private Collection<String> processServers(JsonArray serverStrings) {
        LinkedList<String> validServers = new LinkedList<>();
        for (Object server : serverStrings) {
            if (server == null || !Pattern.matches(VALID_SERVER, (String) server)) {
                log.warn("processServers", "invalidServer", new String[]{"server"}, server);
                continue;
            }

            validServers.add((String) server);
        }

        return validServers;
    }
}
