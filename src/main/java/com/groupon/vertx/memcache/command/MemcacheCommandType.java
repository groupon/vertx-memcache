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

import com.groupon.vertx.memcache.parser.LineParserType;

/**
 * List of Memcache commands.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public enum MemcacheCommandType {
    set(LineParserType.STORE),
    add(LineParserType.STORE),
    replace(LineParserType.STORE),
    append(LineParserType.STORE),
    prepend(LineParserType.STORE),
    get(LineParserType.RETRIEVE),
    delete(LineParserType.DELETE),
    touch(LineParserType.TOUCH),
    incr(LineParserType.MODIFY),
    decr(LineParserType.MODIFY);


    private final String command;
    private final LineParserType lineParserType;

    MemcacheCommandType(LineParserType lineParserType) {
        this(null, lineParserType);
    }

    MemcacheCommandType(String command, LineParserType lineParserType) {
        this.command = command == null ? this.name() : command;
        this.lineParserType = lineParserType;
    }

    public String getCommand() {
        return command;
    }

    public LineParserType getLineParserType() {
        return lineParserType;
    }
}
