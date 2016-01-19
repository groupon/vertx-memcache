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

import java.util.Objects;

/**
 * This record represents an entry for the current server with it's assigned hash value.  There will be one entry for
 * each point assigned to the weighted server.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheServerEntry implements Comparable<MemcacheServerEntry> {
    private final MemcacheServer server;
    private final Long value;

    /**
     * Constructor for building an empty server entry for comparisons.
     *
     * @param value - Long hash value for server
     */
    public MemcacheServerEntry(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("One or more values were null.");
        }

        this.server = null;
        this.value = value;
    }

    /**
     * This constructs the entry with the current server with the hash value for finding the nearest server.
     *
     * @param server - Instance of memcache server
     * @param value - Long hash value for server
     */
    public MemcacheServerEntry(MemcacheServer server, Long value) {
        if (server == null || value == null) {
            throw new IllegalArgumentException("One or more values were null.");
        }

        this.server = server;
        this.value = value;
    }

    public MemcacheServer getServer() {
        return server;
    }

    /**
     * Compares server entries.
     *
     * @param entry - Server to compare against
     * @return result of comparison
     */
    public int compareTo(MemcacheServerEntry entry) {
        if (entry != null) {
            return value.compareTo(entry.value);
        }
        // Sort null entries last
        return -1;
    }

    @Override
    public boolean equals(Object other) {
        // TODO(vkoskela): Fix equals/compareTo. Currently, equals matches compareTo but both ignore the server field. [ISSUE-1]
        if (this == other) {
            return true;
        }
        if (!(other instanceof MemcacheServerEntry)) {
            return false;
        }
        final MemcacheServerEntry otherMemcacheServerEntry = (MemcacheServerEntry) other;
        return Objects.equals(value, otherMemcacheServerEntry.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
