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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.ModifyCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;

/**
 * Tests for <code>ModifyLineParser</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class ModifyLineParserTest {
    private ModifyLineParser parser;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception {
        parser = new ModifyLineParser();
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void testIntegerResult() throws Exception {
        outputStream.write("12345".getBytes());
        assertTrue("Failed to parse value", parser.isResponseEnd(outputStream));

        ModifyCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.success, response.getStatus());
        assertEquals("Value not parsed", Integer.valueOf(12345), response.getData());
    }

    @Test
    public void testNonIntegerResult() throws Exception {
        outputStream.write("hello".getBytes());
        try {
            parser.isResponseEnd(outputStream);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }

    @Test
    public void testNotFoundResult() throws Exception {
        outputStream.write(MemcacheResponseType.NOT_FOUND.type.getBytes());
        assertTrue("Failed to parse value", parser.isResponseEnd(outputStream));

        ModifyCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.success, response.getStatus());
        assertNull("Value should be null", response.getData());
    }

    @Test
    public void testErrorEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.ERROR.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        ModifyCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.ERROR.name(), response.getMessage());
    }

    @Test
    public void testClientErrorEndLine() throws Exception {
        String clientError = "CLIENT ERROR message";
        outputStream.write(clientError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        ModifyCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", clientError, response.getMessage());
    }

    @Test
    public void testServerErrorEndLine() throws Exception {
        String serverError = "SERVER ERROR message";
        outputStream.write(serverError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        ModifyCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", serverError, response.getMessage());
    }

    @Test
    public void testInvalidLine() throws Exception {
        try {
            outputStream.write("foo".getBytes());
            parser.isResponseEnd(outputStream);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }
}
