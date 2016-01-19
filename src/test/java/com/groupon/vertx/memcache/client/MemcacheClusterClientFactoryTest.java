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
package com.groupon.vertx.memcache.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.MemcacheClusterConfig;
import com.groupon.vertx.memcache.MemcacheKeys;
import com.groupon.vertx.memcache.hash.HashAlgorithm;

/**
 * Tests for <code>MemcacheClusterClientFactory</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.1.0
 */
public class MemcacheClusterClientFactoryTest implements MemcacheKeys {
    @Mock
    private EventBus eventBus;

    private JsonObject configObj;
    private MemcacheClusterConfig config;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        configObj = new JsonObject();
        configObj.put(EVENT_BUS_ADDRESS_PREFIX_KEY, "address");

        JsonObject clusterObj = new JsonObject();
        clusterObj.put(SERVERS_KEY, new JsonArray().add("server1"));
        clusterObj.put(NAMESPACE_KEY, "namespace");
        clusterObj.put(POINTS_PER_SERVER, 10);
        clusterObj.put(ALGORITHM_KEY, HashAlgorithm.CRC_HASH.name());

        JsonObject clustersObj = new JsonObject();
        clustersObj.put("clusterA", clusterObj);

        configObj.put(CLUSTERS_KEY, clustersObj);

        config = new MemcacheClusterConfig(configObj);
    }

    @Test
    public void testConstructorFail() {
        try {
            assertNull(new MemcacheClusterClientFactory(null, null));
            fail("Exception was not thrown");
        } catch (MemcacheClientException me) {
            assertNotNull(me);
        }
    }

    @Test
    public void testConstructorConfigFail() {
        try {
            configObj.remove(CLUSTERS_KEY);
            assertNull(new MemcacheClusterClientFactory(eventBus, new MemcacheClusterConfig(configObj)));
            fail("Exception was not thrown");
        } catch (Exception me) {
            assertNotNull(me);
        }
    }

    @Test
    public void testConstructor() throws MemcacheClientException {
        assertNotNull(new MemcacheClusterClientFactory(eventBus, config));
    }

    @Test
    public void testGetClient() throws MemcacheClientException {
        MemcacheClusterClientFactory factory = new MemcacheClusterClientFactory(eventBus, config);
        assertNotNull(factory.getClient("clusterA"));
    }
}
