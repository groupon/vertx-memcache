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
package com.groupon.vertx.memcache.client.response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a memcache get response.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public final class RetrieveCommandResponse extends MemcacheCommandResponse {
    private final Map<String, String> data;

    private RetrieveCommandResponse(Builder builder) {
        super(builder);
        data = Collections.unmodifiableMap(new HashMap<>(builder.data));
    }

    public Map<String, String> getData() {
        return data;
    }

    /**
     * Builder for the RetrieveCommandResponse
     */
    public static class Builder extends AbstractBuilder<Builder, RetrieveCommandResponse> {
        private Map<String, String> data = new HashMap<>();

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setData(Map<String, String> value) {
            if (value == null) {
                data = new HashMap<>();
            } else {
                data = value;
            }
            return self();
        }

        public Builder addData(String key, String value) {
            data.put(key, value);
            return self();
        }

        @Override
        public RetrieveCommandResponse build() {
            return new RetrieveCommandResponse(this);
        }
    }
}
