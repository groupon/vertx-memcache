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
package com.groupon.vertx.memcache.client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

import com.groupon.vertx.memcache.MemcacheUnavailableException;
import com.groupon.vertx.memcache.client.response.MemcacheCommandResponse;
import com.groupon.vertx.utils.Logger;

/**
 * Basic handler for extracting the JsonObject from the message response and passing it to the
 * provided handler.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClientResponseHandler<T extends MemcacheCommandResponse> implements Handler<AsyncResult<Message<T>>> {
    private static final Logger log = Logger.getLogger(MemcacheClientResponseHandler.class);

    private Future<T> result;

    public MemcacheClientResponseHandler(Future<T> result) {
        this.result = result;
    }

    @Override
    public void handle(AsyncResult<Message<T>> message) {
        if (message.succeeded()) {
            result.complete(message.result().body());
        } else {
            MemcacheUnavailableException unavailable = new MemcacheUnavailableException();
            if (message.cause() != null) {
                unavailable.addSuppressed(message.cause());
            }
            log.warn("memcacheClientResponse", "exception", unavailable);
            result.fail(unavailable);
        }
    }
}
