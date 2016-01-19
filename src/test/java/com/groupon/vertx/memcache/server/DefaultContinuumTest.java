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

import java.util.Arrays;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.hash.HashAlgorithm;

/**
 * Tests for <code>DefaultContinuum</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class DefaultContinuumTest {

    private MemcacheServer server1;
    private MemcacheServer server2;
    private MemcacheServer server3;

    @Before
    public void setUp() {
        server1 = new MemcacheServer("localhost:1234:2");
        server2 = new MemcacheServer("localhost:1235:2");
        server3 = new MemcacheServer("localhost:1236:2");
    }

    @Test
    public void testDefaultConstructor() {
        DefaultContinuum continuum = new DefaultContinuum(Arrays.asList(server1, server2, server3),
                HashAlgorithm.FNV1_32_HASH, 1);

        TreeMap<Long, MemcacheServer> map = continuum.getServerContinuum();
        assertEquals("Unexpected server count", 3, map.size());
    }

    @Test
    public void testWithExtraPointsPerServer() {
        DefaultContinuum continuum = new DefaultContinuum(Arrays.asList(server1, server2, server3),
                HashAlgorithm.FNV1_32_HASH, 8);

        TreeMap<Long, MemcacheServer> map = continuum.getServerContinuum();
        assertEquals("Unexpected server count", 24, map.size());
    }
}
