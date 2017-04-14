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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.StoreCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;

/**
 * Tests for <code>StoreLineParser</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class StoreLineParserTest {
    private StoreLineParser parser;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        parser = new StoreLineParser();
    }

    @Test
    public void testStoredEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.STORED.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.success, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.STORED.name(), response.getData());
    }

    @Test
    public void testNotStoredEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.NOT_STORED.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.success, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.NOT_STORED.name(), response.getData());
    }

    @Test
    public void testExistsEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.EXISTS.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.success, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.EXISTS.name(), response.getData());
    }

    @Test
    public void testNotFoundEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.NOT_FOUND.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.success, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.NOT_FOUND.name(), response.getData());
    }

    @Test
    public void testErrorEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.ERROR.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.ERROR.name(), response.getMessage());
    }

    @Test
    public void testClientErrorEndLine() throws Exception {
        String clientError = "CLIENT ERROR message";
        outputStream.write(clientError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", clientError, response.getMessage());
    }

    @Test
    public void testServerErrorEndLine() throws Exception {
        String serverError = "SERVER ERROR message";
        outputStream.write(serverError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        StoreCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", serverError, response.getMessage());
    }

    @Test
    public void testUnexpectedFormat() throws Exception {
        try {
            outputStream.write("foo".getBytes());
            parser.isResponseEnd(outputStream);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }
}
