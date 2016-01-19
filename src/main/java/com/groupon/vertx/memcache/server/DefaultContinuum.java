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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.hash.HashAlgorithm;
import com.groupon.vertx.utils.Logger;

/**
 * Handles the default server distribution.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class DefaultContinuum extends Continuum {
    private static final Logger log = Logger.getLogger(DefaultContinuum.class);
    private static final int CONTINUUM_HASH_LENGTH = 4;

    private MessageDigest digest;

    public DefaultContinuum(Collection<MemcacheServer> servers, HashAlgorithm hashAlgorithm, int pointsPerServer) {
        super(hashAlgorithm);

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nae) {
            throw new MemcacheException(nae.getMessage());
        }

        int totalWeight = 0;
        for (MemcacheServer server : servers) {
            totalWeight += server.getWeight();
        }

        if (servers.size() == 1) {
            getServerContinuum().put(Long.MAX_VALUE, servers.iterator().next());
        } else {
            for (MemcacheServer server : servers) {
                int entries = getServerEntryCount(server, pointsPerServer, servers.size(), totalWeight);
                addServerEntries(server, entries);
            }
        }
    }

    private String getServerKey(MemcacheServer server, int index) {
        return server.getHost() + ":" + server.getPort() + ":" + index;
    }

    protected void addServerEntry(MemcacheServer server, int index) {
        String serverKey = getServerKey(server, index);
        byte[] message = digest.digest(serverKey.getBytes(ENCODING));

        // CS.OFF: MagicNumber
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < CONTINUUM_HASH_LENGTH; i++) {
            hexString.append(String.format("%02x", 0xFF & message[i]));
        }

        long value = Long.parseLong(hexString.toString(), 16);
        getServerContinuum().put(value, server);
        // CS.ON: MagicNumber

        log.info("addServerEntry", "addedEntry", new String[] {"serverKey", "value"}, serverKey, value);
    }
}
