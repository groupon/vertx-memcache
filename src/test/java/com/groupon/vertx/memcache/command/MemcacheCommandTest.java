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
package com.groupon.vertx.memcache.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import com.groupon.vertx.memcache.parser.StoreLineParser;

/**
 * Tests for <code>MemcacheCommand</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheCommandTest {

    @Test
    public void testEmptyJson() {
        try {
            new MemcacheCommand(null);
            assertTrue("Unexpected success", false);
        } catch (IllegalArgumentException iae) {
            assertEquals("Unexpected exception", "Invalid command format", iae.getMessage());
        }
    }

    @Test
    public void testEmptyConstructor() {
        try {
            new MemcacheCommand(null, null, null, null);
            assertTrue("Unexpected success", false);
        } catch (IllegalArgumentException iae) {
            assertEquals("Unexpected exception", "Invalid command format", iae.getMessage());
        }
    }

    @Test
    public void testInvalidJsonCommand() {
        try {
            new MemcacheCommand(new JsonObject("{\"command\":\"foo\",\"key\":\"somekey\",\"value\":\"somevalue\",\"expires\":300}"));
            assertTrue("Unexpected success", false);
        } catch (Exception ex) {
            assertEquals("Unexpected exception", "Invalid or unsupported command provided", ex.getMessage());
        }
    }

    @Test
    public void testInvalidCommand() {
        try {
            new MemcacheCommand(null, "somekey", "somevalue", 300);
            assertTrue("Unexpected success", false);
        } catch (Exception ex) {
            assertEquals("Unexpected exception", "Invalid command format", ex.getMessage());
        }
    }

    @Test
    public void testJsonConstructor() {
        MemcacheCommand command = new MemcacheCommand(new JsonObject("{\"command\":\"set\",\"key\":\"somekey\",\"value\":\"somevalue\",\"expires\":300}"));
        assertEquals("Invalid type", MemcacheCommandType.set, command.getType());
        assertEquals("Invalid command", "set", command.getCommand());
        assertEquals("Invalid key", "somekey", command.getKey());
        assertEquals("Invalid value", "somevalue", command.getValue());
        assertEquals("Invalid expires", 300, (int) command.getExpires());
        assertTrue("Invalid line parser", command.getLineParser() instanceof StoreLineParser);
    }

    @Test
    public void testConstructor() {
        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "somekey", "somevalue", 300);
        assertEquals("Invalid type", MemcacheCommandType.set, command.getType());
        assertEquals("Invalid command", "set", command.getCommand());
        assertEquals("Invalid key", "somekey", command.getKey());
        assertEquals("Invalid value", "somevalue", command.getValue());
        assertEquals("Invalid expires", 300, (int) command.getExpires());
        assertTrue("Invalid line parser", command.getLineParser() instanceof StoreLineParser);
    }

    @Test
    public void testToJson() {
        JsonObject source = new JsonObject("{\"command\":\"set\",\"key\":\"somekey\",\"value\":\"somevalue\",\"expires\":300}");
        MemcacheCommand command = new MemcacheCommand(source);
        assertEquals("Invalid render", source, command.toJson());
    }
}
