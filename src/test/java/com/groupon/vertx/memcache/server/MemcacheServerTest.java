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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for <code>MemcacheServer</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheServerTest {

    @Test
    public void testServerNoPort() {
        MemcacheServer server = new MemcacheServer("localhost");

        assertEquals("Hostname mismatch", "localhost", server.getHost());
        assertEquals("Port mismatch", MemcacheServer.DEFAULT_PORT, server.getPort());
        assertEquals("Weight mismatch", MemcacheServer.DEFAULT_WEIGHT, server.getWeight());
        assertEquals("Server mismatch", "localhost", server.getServer());
    }

    @Test
    public void testServerWithPort() {
        MemcacheServer server = new MemcacheServer("localhost:1234");

        assertEquals("Hostname mismatch", "localhost", server.getHost());
        assertEquals("Port mismatch", 1234, server.getPort());
        assertEquals("Weight mismatch", MemcacheServer.DEFAULT_WEIGHT, server.getWeight());
        assertEquals("Server mismatch", "localhost:1234", server.getServer());
    }

    @Test
    public void testServerWithPortAndWeight() {
        MemcacheServer server = new MemcacheServer("localhost:1234:12");

        assertEquals("Hostname mismatch", "localhost", server.getHost());
        assertEquals("Port mismatch", 1234, server.getPort());
        assertEquals("Weight mismatch", 12, server.getWeight());
        assertEquals("Server mismatch", "localhost:1234:12", server.getServer());
    }
}
