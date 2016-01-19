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

import java.util.ArrayList;

import com.groupon.vertx.memcache.MemcacheConfig;

/**
 * A factory for creating server continuums.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public final class ContinuumFactory {
    private ContinuumFactory() { }

    public static Continuum create(MemcacheConfig config) {
        ArrayList<MemcacheServer> memcacheServerList = new ArrayList<>();
        for (String server : config.getServers()) {
            MemcacheServer memcacheServer = new MemcacheServer(server);
            memcacheServerList.add(memcacheServer);
        }

        if (memcacheServerList.size() < 1) {
            throw new IllegalArgumentException("No servers defined");
        }

        Continuum result;

        switch (config.getContinuumType()) {
            case KETAMA:
                result = new KetamaContinuum(memcacheServerList, config.getHashAlgorithm(), config.getPointsPerServer());
                break;
            default:
                result = new DefaultContinuum(memcacheServerList, config.getHashAlgorithm(), config.getPointsPerServer());
                break;
        }

        return result;
    }
}
