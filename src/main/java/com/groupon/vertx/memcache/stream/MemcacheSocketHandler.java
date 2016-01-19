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
package com.groupon.vertx.memcache.stream;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import com.groupon.vertx.memcache.command.MemcacheCommandHandler;
import com.groupon.vertx.memcache.server.MemcacheServer;
import com.groupon.vertx.utils.Logger;

/**
 * This handler opens the connection to the Memcache server and registers the message handler on
 * success.  If the connection fails or is closed, it unregisters the handler and attempts to
 * reconnect.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheSocketHandler implements Handler<Long> {
    private static final Logger log = Logger.getLogger(MemcacheSocketHandler.class);
    private static final long MAXIMUM_DELAY = 60000;

    private Vertx vertx;
    private String eventBusAddress;
    private MemcacheServer server;
    private NetClient netClient;
    private long delayFactor;
    private long currentDelay;

    public MemcacheSocketHandler(Vertx vertx, String eventBusAddress, MemcacheServer server, NetClient netClient, long delayFactor) {
        this.vertx = vertx;
        this.eventBusAddress = eventBusAddress;
        this.server = server;
        this.netClient = netClient;
        this.delayFactor = delayFactor;
        this.currentDelay = delayFactor;
    }

    public void handle(Long time) {
        final Handler<Long> currentHandler = this;

        log.trace("handle", "establishSocket", new String[] {"eventBusAddress", "server", "delay"}, eventBusAddress, server.getServer(), currentDelay);

        netClient.connect(server.getPort(), server.getHost(), new Handler<AsyncResult<NetSocket>>() {
            public void handle(AsyncResult<NetSocket> socket) {
                log.trace("establishSocket", "handle", new String[] {"eventBusAddress", "server", "status"}, eventBusAddress, server.getServer(), socket.succeeded());
                if (socket.succeeded()) {
                    log.trace("establishSocket", "success");

                    currentDelay = delayFactor;

                    final NetSocket netSocket = socket.result();
                    final MemcacheCommandHandler memcacheHandler = new MemcacheCommandHandler(netSocket);
                    final MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(eventBusAddress, memcacheHandler);

                    netSocket.exceptionHandler(ex -> {
                        log.error("establishSocket", "exception", "unknown", ex);
                        consumer.unregister();
                        memcacheHandler.finish();
                    });

                    netSocket.closeHandler(message -> {
                        log.warn("establishSocket", "socketClosed");
                        consumer.unregister();
                        memcacheHandler.finish();
                        vertx.setTimer(currentDelay, currentHandler);
                    });
                } else {
                    if (socket.result() != null) {
                        log.warn("establishSocket", "closeSocket");
                        socket.result().close();
                    }
                    currentDelay = Math.min(currentDelay * 2, MAXIMUM_DELAY);

                    log.warn("establishSocket", "failed", new String[] {"eventBusAddress", "server"}, eventBusAddress, server.getServer());

                    vertx.setTimer(currentDelay, currentHandler);
                }
            }
        });
    }
}
