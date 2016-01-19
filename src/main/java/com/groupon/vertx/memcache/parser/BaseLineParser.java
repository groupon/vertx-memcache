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

import io.vertx.core.json.JsonObject;

import com.groupon.vertx.memcache.stream.MemcacheResponseType;
import com.groupon.vertx.utils.Logger;

/**
 * This represents the generic response parsing from memcache which can be returned from any request.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public abstract class BaseLineParser implements LineParser {
    private static final Logger log = Logger.getLogger(BaseLineParser.class);
    private static final MemcacheResponseType[] RESPONSE_TYPES = new MemcacheResponseType[] {
        MemcacheResponseType.ERROR, MemcacheResponseType.CLIENT_ERROR, MemcacheResponseType.SERVER_ERROR
    };

    protected JsonObject response = new JsonObject();

    public boolean isResponseEnd(byte[] line) {
        MemcacheResponseType type = getResponseType(RESPONSE_TYPES, line);
        if (type != null) {
            log.trace("isResponseEnd", "error", new String[] {"type"}, type);
            response.put("status", "error");
            response.put("message", new String(line, ENCODING));
            return true;
        } else {
            return false;
        }
    }

    protected MemcacheResponseType getResponseType(MemcacheResponseType[] list, byte[] line) {
        MemcacheResponseType type = null;
        for (MemcacheResponseType responseType : list) {
            if (responseType.matches(line)) {
                log.trace("getResponseType", "foundType", new String[] {"type"}, responseType);
                type = responseType;
                break;
            }
        }
        return type;
    }

    public JsonObject getResponse() {
        if (response.size() == 0) {
            log.error("getResponse", "unexpected", "Response returned unexpectedly");
            response.put("status", "error");
            response.put("message", "Response returned unexpectedly.");
        }

        return response;
    }
}
