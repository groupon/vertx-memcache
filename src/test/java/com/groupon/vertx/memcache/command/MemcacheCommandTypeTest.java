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

import org.junit.Test;

import com.groupon.vertx.memcache.parser.LineParserType;

/**
 * Tests for <code>MemcacheCommandType</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheCommandTypeTest {

    @Test
    public void testMemcacheCommandType() {
        assertEquals("Command doesn't match", "add", MemcacheCommandType.add.getCommand());
        assertEquals("LineParser doesn't match", LineParserType.STORE, MemcacheCommandType.add.getLineParserType());
    }
}
