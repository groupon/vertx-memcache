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

import java.net.HttpURLConnection;

import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for <code>MemcacheUnavailableException</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public class MemcacheUnavailableExceptionTest {
    private MemcacheUnavailableException exception;

    @Before
    public void setUp() {
        exception = new MemcacheUnavailableException();
    }

    @Test
    public void testException() {
        JsonObject json = new JsonObject(exception.getMessage());
        assertEquals("error", json.getString("status"));
        assertEquals(Integer.valueOf(HttpURLConnection.HTTP_UNAVAILABLE), json.getInteger("code"));
    }
}
