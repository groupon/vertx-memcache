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

import java.util.Collection;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

import com.groupon.vertx.memcache.MemcacheConfig;
import com.groupon.vertx.memcache.client.response.DeleteCommandResponse;
import com.groupon.vertx.memcache.client.response.ModifyCommandResponse;
import com.groupon.vertx.memcache.client.response.RetrieveCommandResponse;
import com.groupon.vertx.memcache.client.response.StoreCommandResponse;
import com.groupon.vertx.memcache.client.response.TouchCommandResponse;
import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.command.MemcacheCommandType;
import com.groupon.vertx.memcache.server.Continuum;
import com.groupon.vertx.memcache.server.ContinuumFactory;
import com.groupon.vertx.memcache.server.MemcacheServer;
import com.groupon.vertx.utils.Logger;

/**
 * This is a helper class for constructing the eventbus messages to execute the memcache commands.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClient {
    private static final Logger log = Logger.getLogger(MemcacheClient.class);
    private static final long INFINITE_REPLY_TIMEOUT = Long.MAX_VALUE;
    private Continuum continuum;
    private EventBus eventBus;
    private String eventBusAddress;
    private String namespace;

    public MemcacheClient(EventBus eventBus, MemcacheConfig config) {
        this.eventBus = eventBus;
        this.eventBusAddress = config.getEventBusAddress();
        this.namespace = config.getNamespace();

        log.info("initialize", "createContinuum", new String[]{"servers", "pointsPerServer"}, config.getServers().size(),
                config.getPointsPerServer());
        continuum = ContinuumFactory.create(config);
    }

    public Future<ModifyCommandResponse> incr(String key, long value) {
        return modify(MemcacheCommandType.incr, key, String.valueOf(value));
    }

    public Future<ModifyCommandResponse> decr(String key, long value) {
        return modify(MemcacheCommandType.decr, key, String.valueOf(value));
    }

    public Future<StoreCommandResponse> set(String key, String data, int expires) {
        return store(MemcacheCommandType.set, key, data, expires);
    }

    public Future<StoreCommandResponse> add(String key, String data, int expires) {
        return store(MemcacheCommandType.add, key, data, expires);
    }

    public Future<StoreCommandResponse> replace(String key, String data, int expires) {
        return store(MemcacheCommandType.replace, key, data, expires);
    }

    public Future<ModifyCommandResponse> append(String key, String data) {
        return modify(MemcacheCommandType.append, key, data);
    }

    public Future<ModifyCommandResponse> prepend(String key, String data) {
        return modify(MemcacheCommandType.prepend, key, data);
    }

    public Future<RetrieveCommandResponse> get(String key) {
        return retrieve(MemcacheCommandType.get, key);
    }

    public Future<RetrieveCommandResponse> get(Collection<String> keys) {
        Future<RetrieveCommandResponse> finalResult = Future.future();

        MemcacheClientMultiResponseHandler handleWrapper = new MemcacheClientMultiResponseHandler(finalResult, keys.size());
        for (String key : keys) {
            retrieve(MemcacheCommandType.get, key).setHandler(handleWrapper);
        }

        return finalResult;
    }

    public Future<DeleteCommandResponse> delete(String key) {
        Future<DeleteCommandResponse> finalResult = Future.future();

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.delete, getCacheKey(key), null, null);

        final DeliveryOptions deliveryOptions = new DeliveryOptions()
                .setSendTimeout(INFINITE_REPLY_TIMEOUT);
        eventBus.send(getEventBusAddress(key), command, deliveryOptions,
                new MemcacheClientResponseHandler<>(finalResult));

        return finalResult;
    }

    public Future<TouchCommandResponse> touch(String key, int expires) {
        Future<TouchCommandResponse> finalResult = Future.future();

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.touch, getCacheKey(key), null, expires);

        final DeliveryOptions deliveryOptions = new DeliveryOptions()
                .setSendTimeout(INFINITE_REPLY_TIMEOUT);
        eventBus.send(getEventBusAddress(key), command, deliveryOptions,
                new MemcacheClientResponseHandler<>(finalResult));

        return finalResult;
    }

    public String getNamespace() {
        return this.namespace;
    }

    private Future<RetrieveCommandResponse> retrieve(MemcacheCommandType commandType, String key) {
        Future<RetrieveCommandResponse> finalResult = Future.future();

        MemcacheCommand command = new MemcacheCommand(commandType, getCacheKey(key), null, null);

        final DeliveryOptions deliveryOptions = new DeliveryOptions()
                .setSendTimeout(INFINITE_REPLY_TIMEOUT);
        eventBus.send(getEventBusAddress(key), command, deliveryOptions,
                new TranslateKeyResponseHandler(finalResult, key, command.getKey()));

        return finalResult;
    }

    private Future<ModifyCommandResponse> modify(MemcacheCommandType commandType, String key, String data) {
        Future<ModifyCommandResponse> finalResult = Future.future();

        MemcacheCommand command = new MemcacheCommand(commandType, getCacheKey(key), data, null);

        final DeliveryOptions deliveryOptions = new DeliveryOptions()
                .setSendTimeout(INFINITE_REPLY_TIMEOUT);
        eventBus.send(getEventBusAddress(key), command, deliveryOptions,
                new MemcacheClientResponseHandler<>(finalResult));

        return finalResult;
    }

    private Future<StoreCommandResponse> store(MemcacheCommandType commandType, String key, String data, int expires) {
        Future<StoreCommandResponse> finalResult = Future.future();

        MemcacheCommand command = new MemcacheCommand(commandType, getCacheKey(key), data, expires);

        final DeliveryOptions deliveryOptions = new DeliveryOptions()
                .setSendTimeout(INFINITE_REPLY_TIMEOUT);
        eventBus.send(getEventBusAddress(key), command, deliveryOptions,
                new MemcacheClientResponseHandler<>(finalResult));

        return finalResult;
    }

    private String getCacheKey(String key) {
        if (namespace != null) {
            return namespace + key;
        } else {
            return key;
        }
    }

    private String getEventBusAddress(String key) {
        MemcacheServer server = continuum.getServer(getCacheKey(key));
        log.debug("getEventBusAddress", "serverAddress", new String[]{"address"}, eventBusAddress + "_" + server.getServer());
        return eventBusAddress + "_" + server.getServer();
    }
}
