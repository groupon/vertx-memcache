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
 * Creates a continuum using the Ketama key distribution.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class KetamaContinuum extends Continuum {
    private static final Logger log = Logger.getLogger(KetamaContinuum.class);
    private static final int KETAMA_SAMPLES = 4;

    private MessageDigest digest;

    public KetamaContinuum(Collection<MemcacheServer> servers, HashAlgorithm hashAlgorithm, int pointsPerServer) {
        super(hashAlgorithm);

        try {
            digest = MessageDigest.getInstance("MD5");
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
        if (server.getPort() == MemcacheServer.DEFAULT_PORT) {
            return server.getHost() + "-" + index;
        } else {
            return server.getHost() + ":" + server.getPort() + "-" + index;
        }
    }

    @Override
    protected void addServerEntries(MemcacheServer server, int entries) {
        for (int i = 0; i < ((double) entries / (double) KETAMA_SAMPLES); i++) {
            addServerEntry(server, i);
        }
    }

    protected void addServerEntry(MemcacheServer server, int index) {
        String serverKey = getServerKey(server, index);
        byte[] message = digest.digest(serverKey.getBytes(ENCODING));

        for (int i = 0; i < KETAMA_SAMPLES; i++) {
            // CS.OFF: MagicNumber
            long value = ((long) (message[3 + i * 4] & 0xFF) << 24) | ((long) (message[2 + i * 4] & 0xFF) << 16) |
                    ((long) (message[1 + i * 4] & 0xFF) << 8) | (message[i * 4] & 0xFF);
            getServerContinuum().put(value, server);
            // CS.ON: MagicNumber

            log.info("addServerEntry", "addedEntry", new String[] {"serverKey", "value"}, serverKey, value);
        }
    }
}
