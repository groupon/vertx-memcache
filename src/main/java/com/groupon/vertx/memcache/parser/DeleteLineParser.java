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
package com.groupon.vertx.memcache.parser;

import java.io.ByteArrayOutputStream;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.DeleteCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;
import com.groupon.vertx.utils.Logger;

/**
 * This supports the parsing logic for memcache command DELETE.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class DeleteLineParser extends BaseLineParser<DeleteCommandResponse, DeleteCommandResponse.Builder> {
    private static final Logger log = Logger.getLogger(DeleteLineParser.class);
    private static final MemcacheResponseType[] RESPONSE_TYPES = new MemcacheResponseType[] {
        MemcacheResponseType.DELETED, MemcacheResponseType.NOT_FOUND
    };

    private DeleteCommandResponse.Builder builder;

    public DeleteLineParser() {
        builder = new DeleteCommandResponse.Builder();
    }

    @Override
    protected DeleteCommandResponse.Builder getResponseBuilder() {
        return builder;
    }

    @Override
    public boolean isResponseEnd(ByteArrayOutputStream line) {
        boolean match = super.isResponseEnd(line);
        if (match) {
            return true;
        }

        MemcacheResponseType type = getResponseType(RESPONSE_TYPES, line);
        if (type != null) {
            builder.setStatus(JsendStatus.success);
            builder.setData(type.name());
        } else {
            log.error("isResponseEnd", "exception", "unexpectedFormat", new String[] {"line"}, getMessageNullIfError(line));
            throw new MemcacheException("Unexpected format in response");
        }

        return true;
    }
}
