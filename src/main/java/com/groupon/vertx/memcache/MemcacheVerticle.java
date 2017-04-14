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
package com.groupon.vertx.memcache;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.net.NetClient;

import com.groupon.vertx.memcache.client.response.DeleteCommandResponse;
import com.groupon.vertx.memcache.client.response.MemcacheCommandResponse;
import com.groupon.vertx.memcache.client.response.ModifyCommandResponse;
import com.groupon.vertx.memcache.client.response.RetrieveCommandResponse;
import com.groupon.vertx.memcache.client.response.StoreCommandResponse;
import com.groupon.vertx.memcache.client.response.TouchCommandResponse;
import com.groupon.vertx.memcache.codec.DeleteCommandResponseCodec;
import com.groupon.vertx.memcache.codec.MemcacheCommandCodec;
import com.groupon.vertx.memcache.codec.MemcacheCommandResponseCodec;
import com.groupon.vertx.memcache.codec.ModifyCommandResponseCodec;
import com.groupon.vertx.memcache.codec.RetrieveCommandResponseCodec;
import com.groupon.vertx.memcache.codec.StoreCommandResponseCodec;
import com.groupon.vertx.memcache.codec.TouchCommandResponseCodec;
import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.server.MemcacheServer;
import com.groupon.vertx.memcache.stream.MemcacheSocketHandler;
import com.groupon.vertx.utils.Logger;

/**
 * This launches the handlers to listen for Memcache requests.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheVerticle extends AbstractVerticle implements MemcacheKeys {
    private static final Logger log = Logger.getLogger(MemcacheVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {
        log.info("start", "initializationStarted");

        MemcacheConfig memcacheConfig;

        try {
            memcacheConfig = new MemcacheConfig(config().getJsonObject(MEMCACHE_KEY));
        } catch (MemcacheException me) {
            log.error("start", "exception", me.getMessage());
            startFuture.fail(new Exception(me.getMessage()));
            return;
        }

        try {
            registerDefaultCodec(MemcacheCommand.class, new MemcacheCommandCodec());
            registerDefaultCodec(MemcacheCommandResponse.class, new MemcacheCommandResponseCodec());
            registerDefaultCodec(DeleteCommandResponse.class, new DeleteCommandResponseCodec());
            registerDefaultCodec(ModifyCommandResponse.class, new ModifyCommandResponseCodec());
            registerDefaultCodec(RetrieveCommandResponse.class, new RetrieveCommandResponseCodec());
            registerDefaultCodec(StoreCommandResponse.class, new StoreCommandResponseCodec());
            registerDefaultCodec(TouchCommandResponse.class, new TouchCommandResponseCodec());
        } catch (Exception ex) {
            log.error("start", "exception", ex.getMessage());
            startFuture.fail(new Exception(ex.getMessage()));
            return;
        }

        NetClient netClient = vertx.createNetClient();
        establishSockets(memcacheConfig, netClient);

        log.info("start", "initializationCompleted");
        startFuture.complete();
    }

    private <T> void registerDefaultCodec(Class<T> clazz, MessageCodec<T, ?> codec) {
        try {
            vertx.eventBus().registerDefaultCodec(clazz, codec);
        } catch (IllegalStateException iae) {
            // Codec is already registered.
        }
    }

    /**
     * This method opens the connection to the Memcache server and registers the message handler on
     * success.  If the connection fails or is closed, it unregisters the handler and attempts to
     * reconnect.
     *
     * @param memcacheConfig - The configuration for the connection to Memcache
     * @param netClient      - The client for connecting to Memcache.
     */
    private void establishSockets(final MemcacheConfig memcacheConfig, final NetClient netClient) {
        for (String server : memcacheConfig.getServers()) {
            final String eventBusAddress = memcacheConfig.getEventBusAddress() + "_" + server;
            final MemcacheServer memcacheServer = new MemcacheServer(server);

            MemcacheSocketHandler handler = new MemcacheSocketHandler(vertx, eventBusAddress, memcacheServer, netClient,
                    memcacheConfig.getRetryInterval());
            handler.handle(System.currentTimeMillis());
        }
    }
}
