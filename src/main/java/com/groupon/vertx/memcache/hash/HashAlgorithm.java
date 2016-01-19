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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * List of supported server hashing algorythms.  The implementations for this came from:
 *
 * http://code.google.com/p/spymemcached/
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public enum HashAlgorithm {
    NATIVE_HASH,
    CRC_HASH,
    FNV1_64_HASH,
    FNV1A_64_HASH,
    FNV1_32_HASH,
    FNV1A_32_HASH,
    KETAMA_HASH;

    private static final Charset ENCODING = Charset.forName("UTF-8");

    private static final long FNV_64_INIT = 0xcbf29ce484222325L;
    private static final long FNV_64_PRIME = 0x100000001b3L;

    private static final long FNV_32_INIT = 2166136261L;
    private static final long FNV_32_PRIME = 16777619;

    private static MessageDigest md5Digest = null;
    private static CRC32 crc32 = null;

    static {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }

        crc32 = new CRC32();
    }

    /**
     * Compute the hash for the given key.
     *
     * @param k - String key
     * @return a positive integer hash
     */
    public long hash(final String k) {
        long rv = 0;
        int len = k.length();
        // CS.OFF: MagicNumber
        switch (this) {
            case NATIVE_HASH:
                rv = k.hashCode();
                break;
            case FNV1_64_HASH:
                // Thanks to pierre@demartines.com for the pointer
                rv = FNV_64_INIT;
                for (int i = 0; i < len; i++) {
                    rv *= FNV_64_PRIME;
                    rv ^= k.charAt(i);
                }
                break;
            case FNV1A_64_HASH:
                rv = FNV_64_INIT;
                for (int i = 0; i < len; i++) {
                    rv ^= k.charAt(i);
                    rv *= FNV_64_PRIME;
                }
                break;
            case FNV1_32_HASH:
                rv = FNV_32_INIT;
                for (int i = 0; i < len; i++) {
                    rv *= FNV_32_PRIME;
                    rv ^= k.charAt(i);
                }
                break;
            case FNV1A_32_HASH:
                rv = FNV_32_INIT;
                for (int i = 0; i < len; i++) {
                    rv ^= k.charAt(i);
                    rv *= FNV_32_PRIME;
                }
                break;
            default:
                rv = hash(k.getBytes(ENCODING));
                break;
        }
        return rv & 0xffffffffL; /* Truncate to 32-bits */
        // CS.ON: MagicNumber
    }

    /**
     * Compute the hash for the given key.
     *
     * @param k - byte array key
     * @return a positive integer hash
     */
    private long hash(final byte[] k) {
        long rv = 0;
        // CS.OFF: MagicNumber
        switch (this) {
            case CRC_HASH:
                crc32.reset();
                crc32.update(k);
                rv = (crc32.getValue() >> 16) & 0x7fff;
                break;
            case KETAMA_HASH:
                byte[] bKey = md5Digest.digest(k);
                rv = ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) |
                        ((long) (bKey[1] & 0xFF) << 8) | (bKey[0] & 0xFF);
                break;
            default:
                assert false;
        }
        // CS.ON: MagicNumber
        return rv;
    }
}
