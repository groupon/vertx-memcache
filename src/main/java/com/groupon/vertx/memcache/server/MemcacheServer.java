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

/**
 * Container for holding a server segmented into it's components.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheServer {
    private static final int SERVER_PARTS = 3;
    public static final int DEFAULT_PORT = 11211;
    public static final int DEFAULT_WEIGHT = 8;

    private String host;
    private int port = DEFAULT_PORT;
    private int weight = DEFAULT_WEIGHT;
    private String server;

    public MemcacheServer(String server) {
        this.server = server;

        String[] parts = server.split(":", SERVER_PARTS);
        host = parts[0];
        if (parts.length > 1) {
            port = Integer.parseInt(parts[1]);
        }
        if (parts.length > 2) {
            weight = Integer.parseInt(parts[2]);
        }
    }

    public String getServer() {
        return server;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getWeight() {
        return weight;
    }
}
