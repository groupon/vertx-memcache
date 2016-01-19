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

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.hash.HashAlgorithm;

/**
 * Tests for <code>MemcacheClusterConfig</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClusterConfigTest implements MemcacheKeys {
    @Mock
    private EventBus eventBus;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNullJsonObject() {
        try {
            MemcacheClusterConfig config = new MemcacheClusterConfig(null);
        } catch (MemcacheException me) {
            assertNotNull("Exception expected", me);
        }
    }

    @Test
    public void testEmptyJsonObject() {
        try {
            MemcacheClusterConfig config = new MemcacheClusterConfig(new JsonObject());
        } catch (MemcacheException me) {
            assertNotNull("Exception expected", me);
        }
    }

    @Test
    public void testMinimalJsonObject() {
        JsonObject configObj = new JsonObject();
        configObj.put(EVENT_BUS_ADDRESS_PREFIX_KEY, "address");

        JsonObject clusterObj = new JsonObject();
        clusterObj.put(SERVERS_KEY, new JsonArray().add("server1"));

        JsonObject clustersObj = new JsonObject();
        clustersObj.put("clusterA", clusterObj);

        configObj.put(CLUSTERS_KEY, clustersObj);

        try {
            MemcacheClusterConfig config = new MemcacheClusterConfig(configObj);
            assertEquals("EventBusAddress doesn't match", "address", config.getEventBusAddressPrefix());
            assertEquals(1, config.getClusterNames().size());
            assertEquals(1, config.getServers().size());
            assertEquals("Servers doesn't match", "server1", config.getServers().iterator().next());

            MemcacheConfig memcacheConfig = config.getCluster("clusterA");
            assertNotNull(memcacheConfig);
            assertNull("Namespace shouldn't have a default", memcacheConfig.getNamespace());
            assertEquals("Default points per server doesn't match", 160, memcacheConfig.getPointsPerServer());
            assertEquals("Default hash algorithm doesn't match", HashAlgorithm.FNV1_32_HASH, memcacheConfig.getHashAlgorithm());
        } catch (MemcacheException me) {
            assertNull("Unexpected exception", me);
        }
    }

    @Test
    public void testFullJsonObject() {
        JsonObject configObj = new JsonObject();
        configObj.put(EVENT_BUS_ADDRESS_PREFIX_KEY, "address");

        JsonObject clusterObj = new JsonObject();
        clusterObj.put(SERVERS_KEY, new JsonArray().add("server1"));
        clusterObj.put(NAMESPACE_KEY, "namespace");
        clusterObj.put(POINTS_PER_SERVER, 10);
        clusterObj.put(ALGORITHM_KEY, HashAlgorithm.CRC_HASH.name());

        JsonObject clustersObj = new JsonObject();
        clustersObj.put("clusterA", clusterObj);

        configObj.put(CLUSTERS_KEY, clustersObj);

        try {
            MemcacheClusterConfig config = new MemcacheClusterConfig(configObj);
            assertEquals("EventBusAddress doesn't match", "address", config.getEventBusAddressPrefix());
            assertEquals(1, config.getClusterNames().size());
            assertEquals(1, config.getServers().size());
            assertEquals("Servers doesn't match", "server1", config.getServers().iterator().next());

            MemcacheConfig memcacheConfig = config.getCluster("clusterA");
            assertNotNull(memcacheConfig);
            assertEquals("Namespace doesn't match", "namespace", memcacheConfig.getNamespace());
            assertEquals("Points per server doesn't match", 10, memcacheConfig.getPointsPerServer());
            assertEquals("Hash algorithm doesn't match", HashAlgorithm.CRC_HASH, memcacheConfig.getHashAlgorithm());
        } catch (MemcacheException me) {
            assertNull("Unexpected exception", me);
        }
    }
}
