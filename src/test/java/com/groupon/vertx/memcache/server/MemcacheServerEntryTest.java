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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for <code>MemcacheServerEntry</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheServerEntryTest {
    private MemcacheServerEntry firstEntry;
    private MemcacheServerEntry secondEntry;

    @Before
    public void setUp() {
        firstEntry = new MemcacheServerEntry(111L);
        secondEntry = new MemcacheServerEntry(222L);
    }

    @Test
    public void testConstructor() {
        MemcacheServer server = new MemcacheServer("localhost:1234");
        MemcacheServerEntry entry = new MemcacheServerEntry(server, 1L);

        assertEquals("Server not returned", server, entry.getServer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFailure() {
        new MemcacheServerEntry(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFailure2() {
        new MemcacheServerEntry(null, null);
    }

    @Test
    public void testCompareToLessThan() {
        assertEquals("Invalid ordering", -1, firstEntry.compareTo(secondEntry));
    }

    @Test
    public void testCompareToGreaterThan() {
        assertEquals("Invalid ordering", 1, secondEntry.compareTo(firstEntry));
    }

    @Test
    public void testCompareToEqual() {
        assertEquals("Invalid ordering", 0, firstEntry.compareTo(firstEntry));
    }

    @Test
    public void testCompareToNull() {
        assertEquals("Invalid ordering", -1, firstEntry.compareTo(null));
    }

    @Test
    public void testEqualsSameObject() {
        Object other = firstEntry;
        assertTrue("Invalid equality", firstEntry.equals(other));
    }

    @Test
    public void testEqualsSameValue() {
        Object other = new MemcacheServerEntry(111L);
        assertTrue("Invalid equality", firstEntry.equals(other));
    }

    @Test
    public void testNotEqualsDifferentObject() {
        assertFalse("Invalid equality", firstEntry.equals(secondEntry));
    }

    @Test
    public void testNotEqualsDifferentClass() {
        Object other = 111L;
        assertFalse("Invalid equality", firstEntry.equals(other));
    }

    @Test
    public void testHashCode() {
        assertEquals("Invalid hashcode", Long.valueOf(111).hashCode(), firstEntry.hashCode());
    }
}
