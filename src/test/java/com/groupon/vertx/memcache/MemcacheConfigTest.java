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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.hash.HashAlgorithm;

/**
 * Tests for <code>MemcacheConfig</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheConfigTest implements MemcacheKeys {
    @Mock
    private EventBus eventBus;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNullJsonObject() {
        try {
            MemcacheConfig config = new MemcacheConfig(null);
        } catch (MemcacheException me) {
            assertNotNull("Exception expected", me);
        }
    }

    @Test
    public void testEmptyJsonObject() {
        try {
            MemcacheConfig config = new MemcacheConfig(new JsonObject());
        } catch (MemcacheException me) {
            assertNotNull("Exception expected", me);
        }
    }

    @Test
    public void testMinimalJsonObject() {
        JsonObject configObj = new JsonObject();
        configObj.put(SERVERS_KEY, new JsonArray().add("server1"));
        configObj.put(EVENT_BUS_ADDRESS_KEY, "address");

        try {
            MemcacheConfig config = new MemcacheConfig(configObj);
            assertEquals("Servers doesn't match", Arrays.asList(new String[]{"server1"}), config.getServers());
            assertEquals("EventBusAddress doesn't match", "address", config.getEventBusAddress());
            assertNull("Namespace shouldn't have a default", config.getNamespace());
            assertEquals("Default points per server doesn't match", 160, config.getPointsPerServer());
            assertEquals("Default hash algorithm doesn't match", HashAlgorithm.FNV1_32_HASH, config.getHashAlgorithm());

        } catch (MemcacheException me) {
            assertNull("Unexpected exception", me);
        }
    }

    @Test
    public void testFullJsonObject() {
        JsonObject configObj = new JsonObject();
        configObj.put(SERVERS_KEY, new JsonArray().add("server1"));
        configObj.put(EVENT_BUS_ADDRESS_KEY, "address");
        configObj.put(NAMESPACE_KEY, "namespace");
        configObj.put(POINTS_PER_SERVER, 10);
        configObj.put(ALGORITHM_KEY, HashAlgorithm.CRC_HASH.name());

        try {
            MemcacheConfig config = new MemcacheConfig(configObj);
            assertEquals("Servers doesn't match", Arrays.asList(new String[]{"server1"}), config.getServers());
            assertEquals("EventBusAddress doesn't match", "address", config.getEventBusAddress());
            assertEquals("Namespace doesn't match", "namespace", config.getNamespace());
            assertEquals("Points per server doesn't match", 10, config.getPointsPerServer());
            assertEquals("Hash algorithm doesn't match", HashAlgorithm.CRC_HASH, config.getHashAlgorithm());
        } catch (MemcacheException me) {
            assertNull("Unexpected exception", me);
        }
    }

    @Test
    public void testServerValidation() {
        String[] validServers = new String[]{"server1", "server2:11211", "server3:11211:8", "1.1.1.1", "1.1.1.1:11211", "1.1.1.1:11211:8"};

        JsonArray servers = new JsonArray();
        for (String server : validServers) {
            servers.add(server);
        }
        servers.add("invalid!server");
        servers.add("invalidserver:1:2:3");

        JsonObject configObj = new JsonObject();
        configObj.put(SERVERS_KEY, servers);
        configObj.put(EVENT_BUS_ADDRESS_KEY, "address");

        try {
            MemcacheConfig config = new MemcacheConfig(configObj);
            assertEquals("Servers doesn't match", Arrays.asList(validServers), config.getServers());
            assertEquals("EventBusAddress doesn't match", "address", config.getEventBusAddress());
            assertNull("Namespace shouldn't have a default", config.getNamespace());
            assertEquals("Default points per server doesn't match", 160, config.getPointsPerServer());
            assertEquals("Default hash algorithm doesn't match", HashAlgorithm.FNV1_32_HASH, config.getHashAlgorithm());
        } catch (MemcacheException me) {
            assertNull("Unexpected exception", me);
        }
    }

    @Test
    public void testCreateNullAlgorithm() {
        JsonObject configObj = new JsonObject();
        configObj.put(SERVERS_KEY, new JsonArray().add("server1"));
        configObj.put(EVENT_BUS_ADDRESS_KEY, "address");
        configObj.putNull(ALGORITHM_KEY);

        assertEquals("Wrong hash algorithm", HashAlgorithm.FNV1_32_HASH, new MemcacheConfig(configObj).getHashAlgorithm());
    }
}
