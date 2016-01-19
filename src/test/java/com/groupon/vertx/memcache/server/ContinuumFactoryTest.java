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
import static org.junit.Assert.assertTrue;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.MemcacheConfig;
import com.groupon.vertx.memcache.MemcacheKeys;
import com.groupon.vertx.memcache.hash.HashAlgorithm;

/**
 * Tests for <code>ContinuumFactory</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class ContinuumFactoryTest implements MemcacheKeys {

    private JsonObject configJson;

    @Before
    public void setUp() {
        configJson = new JsonObject();
        configJson.put(SERVERS_KEY, new JsonArray("[\"server:1234:1\",\"server:1235:1\"]"));
        configJson.put(ALGORITHM_KEY, HashAlgorithm.FNV1_32_HASH.name());
        configJson.put(CONTINUUM_KEY, ContinuumType.KETAMA.name());
        configJson.put(EVENT_BUS_ADDRESS_KEY, "address");
        configJson.put(POINTS_PER_SERVER, 1);
    }

    @Test
    public void testCreateKetama() {
        Continuum continuum = ContinuumFactory.create(new MemcacheConfig(configJson));
        assertTrue("Wrong continuum type", continuum instanceof KetamaContinuum);
        assertEquals("Wrong server count", 8, continuum.getServerContinuum().size());
    }

    @Test
    public void testCreateDefault() {
        configJson.put(CONTINUUM_KEY, ContinuumType.DEFAULT.name());
        Continuum continuum = ContinuumFactory.create(new MemcacheConfig(configJson));
        assertTrue("Wrong continuum type", continuum instanceof DefaultContinuum);
        assertEquals("Wrong server count", 2, continuum.getServerContinuum().size());
    }

    @Test
    public void testCreateNullContinuum() {
        configJson.putNull(CONTINUUM_KEY);
        Continuum continuum = ContinuumFactory.create(new MemcacheConfig(configJson));
        assertTrue("Wrong continuum type", continuum instanceof KetamaContinuum);
        assertEquals("Wrong server count", 8, continuum.getServerContinuum().size());
    }
}
