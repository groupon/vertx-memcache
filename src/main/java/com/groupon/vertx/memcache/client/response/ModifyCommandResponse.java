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
 * Represents a memcache modify response.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public final class ModifyCommandResponse extends MemcacheCommandResponse {
    private final Integer data;

    private ModifyCommandResponse(Builder builder) {
        super(builder);
        data = builder.data;
    }

    public Integer getData() {
        return data;
    }

    /**
     * Builder for the ModifyCommandResponse
     */
    public static class Builder extends AbstractBuilder<Builder, ModifyCommandResponse> {
        private Integer data;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setData(Integer value) {
            data = value;
            return self();
        }

        @Override
        public ModifyCommandResponse build() {
            return new ModifyCommandResponse(this);
        }
    }
}
