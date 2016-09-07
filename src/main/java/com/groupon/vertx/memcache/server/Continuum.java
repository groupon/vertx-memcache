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
package com.groupon.vertx.memcache.server;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.hash.HashAlgorithm;
import com.groupon.vertx.utils.Logger;

/**
 * Base class with the shared methods for the continuum.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public abstract class Continuum {
    private static final Logger log = Logger.getLogger(Continuum.class);
    public static final Charset ENCODING = Charset.forName("UTF-8");

    private final TreeMap<Long, MemcacheServer> servers = new TreeMap<>();
    private final HashAlgorithm hashAlgorithm;

    protected Continuum(HashAlgorithm hashAlgorithm) {
        if (hashAlgorithm == null) {
            throw new MemcacheException("Invalid hash algorithm provided");
        }
        this.hashAlgorithm = hashAlgorithm;
    }

    public MemcacheServer getServer(String key) {
        if (servers.size() == 1) {
            log.debug("getServer", "singleServer");
            return servers.firstEntry().getValue();
        } else {
            long hash = hashAlgorithm.hash(key);
            log.debug("getServer", "hashingKey", new String[] {"alorithm", "hash"}, hashAlgorithm.name(), hash);
            Map.Entry<Long, MemcacheServer> serverEntry = servers.ceilingEntry(hash);
            if (serverEntry == null) {
                return servers.firstEntry().getValue();
            }
            return serverEntry.getValue();
        }
    }

    protected int getServerEntryCount(MemcacheServer server, int pointsPerServer, int totalServers, int totalWeight) {
        return (int) (((double) (totalServers * pointsPerServer * server.getWeight())) / (double) totalWeight);
    }

    protected TreeMap<Long, MemcacheServer> getServerContinuum() {
        return servers;
    }

    protected abstract void addServerEntry(MemcacheServer server, int index);

    protected void addServerEntries(MemcacheServer server, int entries) {
        for (int i = 0; i < entries; i++) {
            addServerEntry(server, i);
        }
    }
}
