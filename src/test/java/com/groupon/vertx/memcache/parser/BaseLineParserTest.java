/**
 * Copyright 2014 Groupon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.MemcacheCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;

/**
 * Tests for <code>BaseLineParser</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class BaseLineParserTest {

    private BaseLineParser<MemcacheCommandResponse, MemcacheCommandResponse.Builder> parser;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        parser = new BaseLineParser<MemcacheCommandResponse, MemcacheCommandResponse.Builder>() {
            private MemcacheCommandResponse.Builder builder = new MemcacheCommandResponse.Builder();

            @Override
            protected MemcacheCommandResponse.Builder getResponseBuilder() {
                return builder;
            }
        };
    }

    @Test
    public void testErrorEndLine() throws Exception {
        outputStream.write(MemcacheResponseType.ERROR.type.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        MemcacheCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", MemcacheResponseType.ERROR.name(), response.getMessage());
    }

    @Test
    public void testClientErrorEndLine() throws Exception {
        String clientError = "CLIENT ERROR message";
        outputStream.write(clientError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        MemcacheCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", clientError, response.getMessage());
    }

    @Test
    public void testServerErrorEndLine() throws Exception {
        String serverError = "SERVER ERROR message";
        outputStream.write(serverError.getBytes());
        assertTrue("Failed to identify end", parser.isResponseEnd(outputStream));
        MemcacheCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", serverError, response.getMessage());
    }

    @Test
    public void testEmptyGetResponse() {
        MemcacheCommandResponse response = parser.getResponse();
        assertEquals("Wrong status", JsendStatus.error, response.getStatus());
        assertEquals("Wrong data", "Response returned unexpectedly.", response.getMessage());
    }
}
