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
package com.groupon.vertx.memcache.codec;

import static org.junit.Assert.assertEquals;

import io.vertx.core.buffer.Buffer;
import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.command.MemcacheCommandType;

/**
 * Tests for <code>MemcacheCommandCodec</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public class MemcacheCommandCodecTest {
    private MemcacheCommandCodec codec;
    private MemcacheCommand command;

    @Before
    public void setUp() {
        codec = new MemcacheCommandCodec();
        command = new MemcacheCommand(MemcacheCommandType.add, "key", "value", 1);

    }

    @Test
    public void testName() {
        assertEquals(codec.getClass().getSimpleName(), codec.name());
    }

    @Test
    public void testTransform() {
        assertEquals(command, codec.transform(command));
    }

    @Test
    public void testSystemCodecId() {
        assertEquals(-1, codec.systemCodecID());
    }

    @Test
    public void testEncodeToWireAndDecodeFromWire() {
        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, command);

        MemcacheCommand endCommand = codec.decodeFromWire(0, buffer);

        assertEquals(command.getType(), endCommand.getType());
        assertEquals(command.getCommand(), endCommand.getCommand());
        assertEquals(command.getKey(), endCommand.getKey());
        assertEquals(command.getValue(), endCommand.getValue());
        assertEquals(command.getExpires(), endCommand.getExpires());
    }
}
