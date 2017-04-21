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
package com.groupon.vertx.memcache.stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for <code>MemcacheResponseType</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheResponseTypeTest {
    private Charset encoding = Charset.forName("UTF-8");
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void testMatchExactSuccess() throws Exception {
        outputStream.write("STORED".getBytes(encoding));
        assertTrue("Exact match should have succeeded", MemcacheResponseType.STORED.matches(outputStream));
    }

    @Test
    public void testMatchExcactFailure() throws Exception {
        outputStream.write("STORE".getBytes(encoding));
        assertFalse("Exact match should have failed", MemcacheResponseType.STORED.matches(outputStream));
    }

    @Test
    public void testMatchNonExactSuccess() throws Exception {
        outputStream.write("VALUE ".getBytes(encoding));
        assertTrue("Partial match should have succeeded", MemcacheResponseType.VALUE.matches(outputStream));
    }

    @Test
    public void testMatchNonExactDifferentLengthSuccess() throws Exception {
        outputStream.write("VALUE EXTRA".getBytes(encoding));
        assertTrue("Partial match should have succeeded", MemcacheResponseType.VALUE.matches(outputStream));
    }

    @Test
    public void testMatchNonExcactFailure() throws Exception {
        outputStream.write("NOTIT".getBytes(encoding));
        assertFalse("Exact match should have failed", MemcacheResponseType.VALUE.matches(outputStream));
    }

    @Test
    public void testMatchNonExcactDifferentLengthsFailure() throws Exception {
        outputStream.write("VALU".getBytes(encoding));
        assertFalse("Exact match should have failed", MemcacheResponseType.VALUE.matches(outputStream));
    }
}
