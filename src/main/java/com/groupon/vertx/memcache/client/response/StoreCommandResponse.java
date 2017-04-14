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

/**
 * Represents a memcache store response.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public final class StoreCommandResponse extends MemcacheCommandResponse {
    private final String data;

    private StoreCommandResponse(Builder builder) {
        super(builder);
        data = builder.data;
    }

    public String getData() {
        return data;
    }

    /**
     * Builder for the StoreCommandResponse
     */
    public static class Builder extends AbstractBuilder<Builder, StoreCommandResponse> {
        private String data;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setData(String value) {
            data = value;
            return self();
        }

        @Override
        public StoreCommandResponse build() {
            return new StoreCommandResponse(this);
        }
    }
}
