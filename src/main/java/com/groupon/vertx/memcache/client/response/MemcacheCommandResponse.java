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

import com.groupon.vertx.memcache.client.JsendStatus;

/**
 * Basic memcache response fields.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public class MemcacheCommandResponse {
    private final JsendStatus status;
    private final String message;

    MemcacheCommandResponse(AbstractBuilder<?, ?> builder) {
        this.status = builder.status;
        this.message = builder.message;
    }

    public JsendStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    /**
     * AbstractBuilder for classes extending the MemcacheCommandResponse
     */
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, O>, O extends MemcacheCommandResponse> {
        private JsendStatus status;
        private String message;

        protected abstract B self();

        public B setStatus(JsendStatus value) {
            status = value;
            return self();
        }

        public B setMessage(String value) {
            message = value;
            return self();
        }

        public abstract O build();
    }

    /**
     * Builder for the MemcacheCommandResponse
     */
    public static class Builder extends AbstractBuilder<Builder, MemcacheCommandResponse> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public MemcacheCommandResponse build() {
            return new MemcacheCommandResponse(this);
        }
    }
}
