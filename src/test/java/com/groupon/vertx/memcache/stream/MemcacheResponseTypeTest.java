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

import java.nio.charset.Charset;

import org.junit.Test;

/**
 * Tests for <code>MemcacheResponseType</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheResponseTypeTest {
    private Charset encoding = Charset.forName("UTF-8");

    @Test
    public void testMatchExactSuccess() {
        assertTrue("Exact match should have succeeded", MemcacheResponseType.STORED.matches("STORED".getBytes(encoding)));
    }

    @Test
    public void testMatchExcactFailure() {
        assertFalse("Exact match should have failed", MemcacheResponseType.STORED.matches("STORE".getBytes(encoding)));
    }

    @Test
    public void testMatchNonExactSuccess() {
        assertTrue("Partial match should have succeeded", MemcacheResponseType.VALUE.matches("VALUE ".getBytes(encoding)));
    }

    @Test
    public void testMatchNonExactDifferentLengthSuccess() {
        assertTrue("Partial match should have succeeded", MemcacheResponseType.VALUE.matches("VALUE EXTRA".getBytes(encoding)));
    }

    @Test
    public void testMatchNonExcactFailure() {
        assertFalse("Exact match should have failed", MemcacheResponseType.VALUE.matches("NOTIT".getBytes(encoding)));
    }

    @Test
    public void testMatchNonExcactDifferentLengthsFailure() {
        assertFalse("Exact match should have failed", MemcacheResponseType.VALUE.matches("VALU".getBytes(encoding)));
    }
}
