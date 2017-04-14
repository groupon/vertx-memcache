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
import java.io.UnsupportedEncodingException;

import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.MemcacheCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;
import com.groupon.vertx.utils.Logger;

/**
 * This represents the generic response parsing from memcache which can be returned from any request.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public abstract class BaseLineParser<T extends MemcacheCommandResponse, B extends MemcacheCommandResponse.AbstractBuilder<B, T>> implements LineParser {
    private static final Logger log = Logger.getLogger(BaseLineParser.class);
    private static final MemcacheResponseType[] RESPONSE_TYPES = new MemcacheResponseType[] {
        MemcacheResponseType.ERROR, MemcacheResponseType.CLIENT_ERROR, MemcacheResponseType.SERVER_ERROR
    };

    protected abstract B getResponseBuilder();

    public boolean isResponseEnd(ByteArrayOutputStream line) {
        try {
            MemcacheResponseType type = getResponseType(RESPONSE_TYPES, line);
            if (type != null) {
                log.trace("isResponseEnd", "error", new String[]{"type"}, type);
                B builder = getResponseBuilder();
                builder.setStatus(JsendStatus.error);
                builder.setMessage(line.toString(ENCODING));
                return true;
            } else {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            log.error("getResponse", "unexpected", "unsupported encoding", e);
            return false;
        }
    }

    protected MemcacheResponseType getResponseType(MemcacheResponseType[] list, ByteArrayOutputStream line) {
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

    @Override
    public T getResponse() {
        B builder = getResponseBuilder();
        T response = builder.build();
        if (response.getStatus() == null) {
            log.error("getResponse", "unexpected", "Response returned unexpectedly");
            builder.setStatus(JsendStatus.error);
            builder.setMessage("Response returned unexpectedly.");
            response = builder.build();
        }

        return response;
    }

    protected String getMessageNullIfError(ByteArrayOutputStream line) {
        try {
            return line.toString(ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.warn("lint_to_string", e.getMessage());
        }
        return null;
    }
}
