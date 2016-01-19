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
package com.groupon.vertx.memcache.hash;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for <code>HashAlgorithm</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class HashAlgorithmTest {

    private String testString = "test";
    private long nativeHash = 3556498L;
    private long fnv164Hash = 2680995689L;
    private long fnv1A64Hash = 427567909L;
    private long fnv132Hash = 3157003241L;
    private long fnv1A32Hash = 2949673445L;
    private long crcHash = 22655L;
    private long ketamaHash = 3446378249L;

    @Test
    public void testNativeHash() {
        assertEquals("Hash doesn't match", nativeHash, HashAlgorithm.NATIVE_HASH.hash(testString));
    }

    @Test
    public void testFnv164Hash() {
        assertEquals("Hash doesn't match", fnv164Hash, HashAlgorithm.FNV1_64_HASH.hash(testString));
    }

    @Test
    public void testFnv1A64Hash() {
        assertEquals("Hash doesn't match", fnv1A64Hash, HashAlgorithm.FNV1A_64_HASH.hash(testString));
    }

    @Test
    public void testFnv132Hash() {
        assertEquals("Hash doesn't match", fnv132Hash, HashAlgorithm.FNV1_32_HASH.hash(testString));
    }

    @Test
    public void testFnv1A32Hash() {
        assertEquals("Hash doesn't match", fnv1A32Hash, HashAlgorithm.FNV1A_32_HASH.hash(testString));
    }

    @Test
    public void testCrcHash() {
        assertEquals("Hash doesn't match", crcHash, HashAlgorithm.CRC_HASH.hash(testString));
    }

    @Test
    public void testKetamaHash() {
        assertEquals("Hash doesn't match", ketamaHash, HashAlgorithm.KETAMA_HASH.hash(testString));
    }
}
