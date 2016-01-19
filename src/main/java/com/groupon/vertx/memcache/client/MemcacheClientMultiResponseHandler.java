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

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.Logger;

/**
 * The purpose of this handler is to store the results of multiple async command requests and return
 * once they have all been completed.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClientMultiResponseHandler implements Handler<AsyncResult<JsonObject>> {
    private static final Logger log = Logger.getLogger(MemcacheClientMultiResponseHandler.class);

    private final Future<JsonObject> result;
    private final AtomicInteger counter;
    private final JsonArray successResults;
    private final JsonArray errorResults;

    private int success = 0;
    private int failure = 0;
    private int error = 0;

    public MemcacheClientMultiResponseHandler(Future<JsonObject> result, int totalCommands) {
        this.result = result;
        this.counter = new AtomicInteger(totalCommands);
        this.successResults = new JsonArray();
        this.errorResults = new JsonArray();
    }

    /**
     * This handles the incoming Memcache command JSON.
     *
     * @param command - The JsonObject containing the command and arguments to send to Memcache.
     */
    public void handle(final AsyncResult<JsonObject> command) {
        if (command.succeeded()) {
            JsonObject body = command.result();
            if (body == null || body.size() == 0) {
                log.warn("handleCommand", "queueFailure", new String[]{"reason"}, "Missing message body");
                failure++;
            } else {
                log.trace("handleCommand", "queueReply", new String[]{"response"}, body);
                String status = body.getString("status");
                if ("success".equals(status)) {
                    JsonObject data = body.getJsonObject("data");
                    if (data != null) {
                        successResults.add(data);
                    } else {
                        successResults.addNull();
                    }
                    success++;
                } else if ("fail".equals(status)) {
                    failure++;
                } else {
                    errorResults.add(body.getString("message"));
                    error++;
                }
            }
        } else {
            log.warn("handleCommand", "queueFailure", command.cause());
            failure++;
        }

        if (counter.decrementAndGet() == 0) {
            result.complete(buildReply());
        }
    }

    private JsonObject buildReply() {
        JsonObject reply = new JsonObject();

        if ((failure + error) == 0 || success > 0) {
            reply.put("status", "success");
            JsonObject data = new JsonObject();
            for (int i = 0; i < successResults.size(); i++) {
                if (successResults.getJsonObject(i) != null) {
                    data.mergeIn(successResults.getJsonObject(i));
                }
            }
            reply.put("data", data);
        } else {
            reply.put("status", "error");
            reply.put("data", errorResults);
        }

        return reply;
    }
}
