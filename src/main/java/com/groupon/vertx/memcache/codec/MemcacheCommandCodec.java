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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.CodecManager;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.command.MemcacheCommandType;

/**
 * A MessageCodec for passing MemcacheComands in Vertx
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public class MemcacheCommandCodec implements MessageCodec<MemcacheCommand, MemcacheCommand> {
    @Override
    public void encodeToWire(Buffer buffer, MemcacheCommand memcacheCommand) {
        JsonObject json = new JsonObject();
        json.put("key", memcacheCommand.getKey());
        json.put("value", memcacheCommand.getValue());
        json.put("expires", memcacheCommand.getExpires());
        json.put("type", memcacheCommand.getType());

        CodecManager.JSON_OBJECT_MESSAGE_CODEC.encodeToWire(buffer, json);
    }

    @Override
    public MemcacheCommand decodeFromWire(int i, Buffer buffer) {
        JsonObject json = CodecManager.JSON_OBJECT_MESSAGE_CODEC.decodeFromWire(i, buffer);

        String key = json.getString("key");
        String value = json.getString("value");
        Integer expires = json.getInteger("expires");
        MemcacheCommandType type = MemcacheCommandType.valueOf(json.getString("type"));

        return new MemcacheCommand(type, key, value, expires);
    }

    @Override
    public MemcacheCommand transform(MemcacheCommand memcacheCommand) {
        return memcacheCommand;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
