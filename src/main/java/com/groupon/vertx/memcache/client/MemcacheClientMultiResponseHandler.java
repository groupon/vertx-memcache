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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import com.groupon.vertx.memcache.client.response.RetrieveCommandResponse;
import com.groupon.vertx.utils.Logger;

/**
 * The purpose of this handler is to store the results of multiple async command requests and return
 * once they have all been completed.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClientMultiResponseHandler implements Handler<AsyncResult<RetrieveCommandResponse>> {
    private static final Logger log = Logger.getLogger(MemcacheClientMultiResponseHandler.class);

    private final Future<RetrieveCommandResponse> result;
    private final AtomicInteger counter;
    private final RetrieveCommandResponse.Builder builder;
    private final List<String> errors;

    private int success = 0;
    private int failure = 0;

    public MemcacheClientMultiResponseHandler(Future<RetrieveCommandResponse> result, int totalCommands) {
        this.result = result;
        this.counter = new AtomicInteger(totalCommands);
        this.builder = new RetrieveCommandResponse.Builder();
        this.errors = new ArrayList<>();
    }

    /**
     * This handles the incoming Memcache command JSON.
     *
     * @param command - The JsonObject containing the command and arguments to send to Memcache.
     */
    public void handle(final AsyncResult<RetrieveCommandResponse> command) {
        if (command.succeeded()) {
            RetrieveCommandResponse body = command.result();
            if (body == null) {
                log.warn("handleCommand", "queueFailure", new String[]{"reason"}, "Missing message body");
                failure++;
            } else {
                log.trace("handleCommand", "queueReply", new String[]{"response"}, body);
                switch (body.getStatus()) {
                    case success:
                        for (Map.Entry<String, String> entry : body.getData().entrySet()) {
                            builder.addData(entry.getKey(), entry.getValue());
                        }
                        success++;
                        break;
                    case fail:
                        failure++;
                        break;
                    default:
                        if (body.getMessage() != null) {
                            errors.add(body.getMessage());
                        }
                        break;
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

    private RetrieveCommandResponse buildReply() {
        if ((failure + errors.size()) == 0 || success > 0) {
            builder.setStatus(JsendStatus.success);
        } else {
            builder.setStatus(JsendStatus.error);
            builder.setMessage(String.join(", ", errors));
        }

        return builder.build();
    }
}
