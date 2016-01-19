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
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.memcache.MemcacheUnavailableException;
import com.groupon.vertx.utils.Logger;

/**
 * Maps the cache keys used in memcache back to the original keys used for the request.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class TranslateKeyResponseHandler implements Handler<AsyncResult<Message<JsonObject>>> {
    private static final Logger log = Logger.getLogger(TranslateKeyResponseHandler.class);

    private Future<JsonObject> result;
    private String key;
    private String cacheKey;

    public TranslateKeyResponseHandler(Future<JsonObject> result, String key, String cacheKey) {
        this.result = result;
        this.key = key;
        this.cacheKey = cacheKey;
    }

    public void handle(AsyncResult<Message<JsonObject>> message) {
        if (message.succeeded()) {
            if (!key.equals(cacheKey)) {
                JsonObject body = message.result().body();
                if (body != null) {
                    Object data = body.getValue("data");
                    if (data instanceof JsonObject) {
                        JsonObject dataJson = (JsonObject) data;

                        Object value = dataJson.getValue(cacheKey);
                        dataJson.remove(cacheKey);
                        if (value != null) {
                            dataJson.put(key, value);
                        }
                    }
                }
            }
            result.complete(message.result().body());
        } else {
            MemcacheUnavailableException unavailable = new MemcacheUnavailableException();
            if (message.cause() != null) {
                unavailable.addSuppressed(message.cause());
            }
            log.warn("translateKeyResponse", "exception", unavailable);
            result.fail(unavailable);
        }
    }
}
