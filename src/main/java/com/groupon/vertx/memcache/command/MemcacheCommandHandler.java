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
package com.groupon.vertx.memcache.command;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetSocket;

import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.MemcacheCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheSocket;
import com.groupon.vertx.utils.Logger;

/**
 * This handler listens for messages and sends commands to the Memcache server.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheCommandHandler implements Handler<Message<MemcacheCommand>> {
    private static final Logger log = Logger.getLogger(MemcacheCommandHandler.class);
    private final MemcacheSocket socket;

    /**
     * This handler listens for messages and sends commands to the Memcache server.  The response
     * from the handler will be a JsonObject with the JSend format:
     * <br>
     * <code>
     * {
     *   'status': 'success',
     *   'data': null
     * }
     * </code>
     * <br>
     * or
     * <br>
     * <code>
     * {
     *   'status': 'fail',
     *   'data': {
     *     'command': 'GET',
     *     'arguments': 'somekey'
     *   }
     * }
     * </code>
     * <br>
     * or
     * <br>
     * <code>
     * {
     *   'status': 'error',
     *   'message': 'A server error occured'
     * }
     * </code>
     *
     * @param socket - The NetSocket which is currently connected to the Memcache server.
     */
    public MemcacheCommandHandler(NetSocket socket) {
        this.socket = new MemcacheSocket(socket);
    }

    /**
     * This handles the incoming Memcache command JSON.
     *
     * @param command - The JsonObject containing the command and arguments to send to Memcache.
     */
    public void handle(final Message<MemcacheCommand> command) {
        MemcacheCommand memcacheCommand = command.body();
        if (memcacheCommand == null) {
            log.warn("handleCommand", "failure", new String[]{"reason"}, "Missing message body");
            command.reply(buildErrorReply("Invalid message with null or empty."));
            return;
        }

        memcacheCommand.commandResponseHandler(commandResponse -> {
            log.trace("handleCommand", "reply", new String[]{"response"}, commandResponse);
            command.reply(commandResponse);
        });

        socket.sendCommand(memcacheCommand);
    }

    public void finish() {
        try {
            socket.close();
        } catch (Exception ex) {
            log.error("reset", "exception", "closingSocket", ex);
        }
    }

    private MemcacheCommandResponse buildErrorReply(String message) {
        return new MemcacheCommandResponse.Builder()
                .setStatus(JsendStatus.error)
                .setMessage(message)
                .build();
    }
}
