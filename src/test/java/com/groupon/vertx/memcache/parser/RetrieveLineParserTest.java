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
package com.groupon.vertx.memcache.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;

/**
 * Tests for <code>RetrieveLineParser</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class RetrieveLineParserTest {
    private RetrieveLineParser parser;
    private Field expectedBytes;
    private Field bytesRetrieved;

    @Before
    public void setUp() throws Exception {
        parser = new RetrieveLineParser();

        expectedBytes = RetrieveLineParser.class.getDeclaredField("expectedBytes");
        expectedBytes.setAccessible(true);

        bytesRetrieved = RetrieveLineParser.class.getDeclaredField("bytesRetrieved");
        bytesRetrieved.setAccessible(true);
    }

    @Test
    public void testValueParseLine() throws Exception {
        byte[] start = (MemcacheResponseType.VALUE.name() + " key 0 4").getBytes();
        assertFalse("Failed to parse value header", parser.isResponseEnd(start));
        assertNotNull("Invalid expected bytes", expectedBytes.get(parser));
        assertEquals("Invalid expected bytes length", 4, ((byte[]) expectedBytes.get(parser)).length);
        assertEquals("Invalid bytes retrieved", 0, bytesRetrieved.getInt(parser));
    }

    @Test
    public void testIncompletePartialParseLine() throws Exception {
        byte[] start = (MemcacheResponseType.VALUE.name() + " key 0 4").getBytes();
        byte[] body = "tes".getBytes();
        assertFalse("Failed to parse value header", parser.isResponseEnd(start));
        assertFalse("Failed to parse value line", parser.isResponseEnd(body));
        assertEquals("Invalid expected bytes", "tes", new String((byte[]) expectedBytes.get(parser)).trim());
        assertEquals("Invalid bytes retrieved", 3, bytesRetrieved.getInt(parser));
    }

    @Test
    public void testPartialParseLine() throws Exception {
        byte[] start = (MemcacheResponseType.VALUE.name() + " key 0 4").getBytes();
        byte[] body = "test".getBytes();
        assertFalse("Failed to parse value header", parser.isResponseEnd(start));
        assertFalse("Failed to parse value line", parser.isResponseEnd(body));
        assertNull("Invalid expected bytes", expectedBytes.get(parser));
        assertEquals("Invalid bytes retrieved", 0, bytesRetrieved.getInt(parser));

        JsonObject response = parser.getResponse();
        assertNull("Wrong status", response.getString("status"));

        JsonObject data = response.getJsonObject("data");
        assertEquals("Value not parsed", "test", data.getString("key"));
    }

    @Test
    public void testCompleteParseLine() throws Exception {
        byte[] start = (MemcacheResponseType.VALUE.name() + " key 0 4").getBytes();
        byte[] body = "test".getBytes();
        byte[] end = MemcacheResponseType.END.type;
        assertFalse("Failed to parse value header", parser.isResponseEnd(start));
        assertFalse("Failed to parse value line", parser.isResponseEnd(body));
        assertTrue("Failed to parse end line", parser.isResponseEnd(end));
        assertNull("Invalid expected bytes", expectedBytes.get(parser));
        assertEquals("Invalid bytes retrieved", 0, bytesRetrieved.getInt(parser));

        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "success", response.getString("status"));

        JsonObject data = response.getJsonObject("data");
        assertEquals("Value not parsed", "test", data.getString("key"));
    }

    @Test
    public void testErrorEndLine() {
        assertTrue("Failed to identify end", parser.isResponseEnd(MemcacheResponseType.ERROR.type));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", MemcacheResponseType.ERROR.name(), response.getString("message"));
    }

    @Test
    public void testClientErrorEndLine() {
        String clientError = "CLIENT ERROR message";
        assertTrue("Failed to identify end", parser.isResponseEnd(clientError.getBytes()));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", clientError, response.getString("message"));
    }

    @Test
    public void testServerErrorEndLine() {
        String serverError = "SERVER ERROR message";
        assertTrue("Failed to identify end", parser.isResponseEnd(serverError.getBytes()));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", serverError, response.getString("message"));
    }

    @Test
    public void testInvalidValue() {
        try {
            byte[] start = (MemcacheResponseType.VALUE.name() + " key").getBytes();
            parser.isResponseEnd(start);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }

    @Test
    public void testValueTooLong() {
        try {
            byte[] start = (MemcacheResponseType.VALUE.name() + " key 0 4").getBytes();
            byte[] body = "testtoolong".getBytes();
            assertFalse("Failed to parse value header", parser.isResponseEnd(start));
            parser.isResponseEnd(body);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Length of value exceeds expected response", me.getMessage());
        }
    }

    @Test
    public void testInvalidLine() {
        try {
            byte[] start = "foo".getBytes();
            parser.isResponseEnd(start);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }
}
