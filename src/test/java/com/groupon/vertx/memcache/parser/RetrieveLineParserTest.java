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

import java.io.ByteArrayOutputStream;
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
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception {
        parser = new RetrieveLineParser();

        expectedBytes = RetrieveLineParser.class.getDeclaredField("expectedBytes");
        expectedBytes.setAccessible(true);

        bytesRetrieved = RetrieveLineParser.class.getDeclaredField("bytesRetrieved");
        bytesRetrieved.setAccessible(true);
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void testValueParseLine() throws Exception {
        outputStream.write((MemcacheResponseType.VALUE.name() + " key 0 4").getBytes());
        assertFalse("Failed to parse value header", parser.isResponseEnd(outputStream));
        assertNotNull("Invalid expected bytes", expectedBytes.get(parser));
        assertEquals("Invalid expected bytes length", 4, ((byte[]) expectedBytes.get(parser)).length);
        assertEquals("Invalid bytes retrieved", 0, bytesRetrieved.getInt(parser));
    }

    @Test
    public void testIncompletePartialParseLine() throws Exception {
        outputStream.write((MemcacheResponseType.VALUE.name() + " key 0 4").getBytes());
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write("tes".getBytes());
        assertFalse("Failed to parse value header", parser.isResponseEnd(outputStream));
        assertFalse("Failed to parse value line", parser.isResponseEnd(body));
        assertEquals("Invalid expected bytes", "tes", new String((byte[]) expectedBytes.get(parser)).trim());
        assertEquals("Invalid bytes retrieved", 3, bytesRetrieved.getInt(parser));
    }

    @Test
    public void testPartialParseLine() throws Exception {
        outputStream.write((MemcacheResponseType.VALUE.name() + " key 0 4").getBytes());
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write("test".getBytes());
        assertFalse("Failed to parse value header", parser.isResponseEnd(outputStream));
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
        outputStream.write((MemcacheResponseType.VALUE.name() + " key 0 4").getBytes());
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write("test".getBytes());
        ByteArrayOutputStream end = new ByteArrayOutputStream();
        end.write(MemcacheResponseType.END.type.getBytes());
        assertFalse("Failed to parse value header", parser.isResponseEnd(outputStream));
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
    public void testErrorEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.ERROR.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", MemcacheResponseType.ERROR.name(), response.getString("message"));
    }

    @Test
    public void testClientErrorEndLine() throws Exception {
        String clientError = "CLIENT ERROR message";
        outputStream.write(clientError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", clientError, response.getString("message"));
    }

    @Test
    public void testServerErrorEndLine() throws Exception {
        String serverError = "SERVER ERROR message";
        outputStream.write(serverError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", serverError, response.getString("message"));
    }

    @Test
    public void testInvalidValue() throws Exception {
        try {
            outputStream.write((MemcacheResponseType.VALUE.name() + " key").getBytes());
            parser.isResponseEnd(outputStream);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }

    @Test
    public void testValueTooLong() throws Exception {
        try {
            outputStream.write((MemcacheResponseType.VALUE.name() + " key 0 4").getBytes());
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write("testtoolong".getBytes());
            assertFalse("Failed to parse value header", parser.isResponseEnd(outputStream));
            parser.isResponseEnd(body);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Length of value exceeds expected response", me.getMessage());
        }
    }

    @Test
    public void testInvalidLine() throws Exception {
        try {
            ByteArrayOutputStream start = new ByteArrayOutputStream();
            start.write("foo".getBytes());
            parser.isResponseEnd(start);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }
}
