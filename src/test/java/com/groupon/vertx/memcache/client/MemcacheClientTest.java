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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hamcrest.FeatureMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Equals;

import com.groupon.vertx.memcache.MemcacheConfig;
import com.groupon.vertx.memcache.MemcacheKeys;
import com.groupon.vertx.memcache.TestMessage;
import com.groupon.vertx.memcache.client.response.DeleteCommandResponse;
import com.groupon.vertx.memcache.client.response.ModifyCommandResponse;
import com.groupon.vertx.memcache.client.response.RetrieveCommandResponse;
import com.groupon.vertx.memcache.client.response.StoreCommandResponse;
import com.groupon.vertx.memcache.client.response.TouchCommandResponse;
import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.command.MemcacheCommandType;
import com.groupon.vertx.memcache.hash.HashAlgorithm;

/**
 * Tests for <code>MemcacheClient</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClientTest implements MemcacheKeys {

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<MemcacheClientResponseHandler<ModifyCommandResponse>> modifyCaptor;

    @Captor
    private ArgumentCaptor<MemcacheClientResponseHandler<StoreCommandResponse>> storeCaptor;

    @Captor
    private ArgumentCaptor<TranslateKeyResponseHandler> getCaptor;

    @Captor
    private ArgumentCaptor<MemcacheClientResponseHandler<DeleteCommandResponse>> deleteCaptor;

    @Captor
    private ArgumentCaptor<MemcacheClientResponseHandler<TouchCommandResponse>> touchCaptor;

    @Captor
    private ArgumentCaptor<MemcacheCommand> commandCaptor;

    private MemcacheClient client;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        JsonObject configObj = new JsonObject();
        configObj.put(SERVERS_KEY, new JsonArray().add("server1"));
        configObj.put(EVENT_BUS_ADDRESS_KEY, "address");
        configObj.put(NAMESPACE_KEY, "namespace");
        configObj.put(POINTS_PER_SERVER, 10);
        configObj.put(ALGORITHM_KEY, HashAlgorithm.CRC_HASH.name());

        MemcacheConfig config = new MemcacheConfig(configObj);
        client = new MemcacheClient(eventBus, config);
    }

    @Test
    public void testIncr() {
        final ModifyCommandResponse response = new ModifyCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.incr("key", 1).setHandler(new Handler<AsyncResult<ModifyCommandResponse>>() {
            @Override
            public void handle(AsyncResult<ModifyCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.incr, "namespacekey", "1", null);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), modifyCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        modifyCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testDecr() {
        final ModifyCommandResponse response = new ModifyCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.decr("key", 1).setHandler(new Handler<AsyncResult<ModifyCommandResponse>>() {
            @Override
            public void handle(AsyncResult<ModifyCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.decr, "namespacekey", "1", null);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), modifyCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        modifyCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testSet() {
        final StoreCommandResponse response = new StoreCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.set("key", "value", 100).setHandler(new Handler<AsyncResult<StoreCommandResponse>>() {
            @Override
            public void handle(AsyncResult<StoreCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "namespacekey", "value", 100);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), storeCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        storeCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testAdd() {
        final StoreCommandResponse response = new StoreCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.add("key", "value", 100).setHandler(new Handler<AsyncResult<StoreCommandResponse>>() {
            @Override
            public void handle(AsyncResult<StoreCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.add, "namespacekey", "value", 100);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE),
                storeCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        storeCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testReplace() {
        final StoreCommandResponse response = new StoreCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.replace("key", "value", 100).setHandler(new Handler<AsyncResult<StoreCommandResponse>>() {
            @Override
            public void handle(AsyncResult<StoreCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.replace, "namespacekey", "value", 100);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), storeCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        storeCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testAppend() {
        final ModifyCommandResponse response = new ModifyCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.append("key", "value").setHandler(new Handler<AsyncResult<ModifyCommandResponse>>() {
            @Override
            public void handle(AsyncResult<ModifyCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.append, "namespacekey", "value", null);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), modifyCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        modifyCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testPrepend() {
        final ModifyCommandResponse response = new ModifyCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.prepend("key", "value").setHandler(new Handler<AsyncResult<ModifyCommandResponse>>() {
            @Override
            public void handle(AsyncResult<ModifyCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.prepend, "namespacekey", "value", null);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), modifyCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        modifyCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testGet() {
        final RetrieveCommandResponse response = new RetrieveCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .setData(Collections.singletonMap("namespacekey", "value"))
                .build();

        client.get("key").setHandler(new Handler<AsyncResult<RetrieveCommandResponse>>() {
            @Override
            public void handle(AsyncResult<RetrieveCommandResponse> result) {
                assertEquals("Result status doesn't match", response.getStatus(), result.result().getStatus());
                assertEquals("Result data doesn't match", Collections.singletonMap("key", "value"), result.result().getData());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "namespacekey", null, null);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), getCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        getCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testMultiGet() {
        final Map<String, String> keyMap = new HashMap<>();
        keyMap.put("namespacekey1", "value");
        keyMap.put("namespacekey2", "value");
        final RetrieveCommandResponse responseFull = new RetrieveCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .setData(keyMap)
                .build();
        List<TestMessage<RetrieveCommandResponse>> messages = Arrays.asList(
                new TestMessage<>(
                        new RetrieveCommandResponse.Builder()
                                .setStatus(JsendStatus.success)
                                .setData(Collections.singletonMap("namespacekey1", keyMap.get("namespacekey1")))
                                .build()),
                new TestMessage<>(
                        new RetrieveCommandResponse.Builder()
                                .setStatus(JsendStatus.success)
                                .setData(Collections.singletonMap("namespacekey2", keyMap.get("namespacekey2")))
                                .build()));

        client.get(Arrays.asList("key1", "key2")).setHandler(new Handler<AsyncResult<RetrieveCommandResponse>>() {
            @Override
            public void handle(AsyncResult<RetrieveCommandResponse> result) {
                assertEquals("Result status doesn't match", responseFull.getStatus(), result.result().getStatus());
                assertEquals("Result data doesn't match", "value", result.result().getData().get("key1"));
                assertEquals("Result data doesn't match", "value", result.result().getData().get("key2"));
            }
        });

        MemcacheCommand command1 = new MemcacheCommand(MemcacheCommandType.get, "namespacekey1", null, null);
        MemcacheCommand command2 = new MemcacheCommand(MemcacheCommandType.get, "namespacekey2", null, null);

        List<MemcacheCommand> expectedCommands = Arrays.asList(command1, command2);

        verify(eventBus, times(2)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), getCaptor.capture());

        List<MemcacheCommand> commands = commandCaptor.getAllValues();
        List<TranslateKeyResponseHandler> handlers = getCaptor.getAllValues();
        for (int i = 0; i < getCaptor.getAllValues().size(); i++) {
            verifyCommand(expectedCommands.get(i), commands.get(i));
            handlers.get(i).handle(Future.succeededFuture(messages.get(i)));
        }
    }

    @Test
    public void testMultiGetWithMissingData() {
        final RetrieveCommandResponse response = new RetrieveCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .setData(Collections.singletonMap("namespacekey1", "value"))
                .build();
        List<TestMessage<RetrieveCommandResponse>> messages = Arrays.asList(
                new TestMessage<>(response),
                new TestMessage<>(new RetrieveCommandResponse.Builder()
                        .setStatus(JsendStatus.success)
                        .setData(Collections.emptyMap())
                        .build()));

        client.get(Arrays.asList("key1", "key2")).setHandler(new Handler<AsyncResult<RetrieveCommandResponse>>() {
            @Override
            public void handle(AsyncResult<RetrieveCommandResponse> result) {
                assertEquals("Result status doesn't match", response.getStatus(), result.result().getStatus());
                assertEquals("Result data doesn't match", Collections.singletonMap("key1", "value"), result.result().getData());
            }
        });

        MemcacheCommand command1 = new MemcacheCommand(MemcacheCommandType.get, "namespacekey1", null, null);
        MemcacheCommand command2 = new MemcacheCommand(MemcacheCommandType.get, "namespacekey2", null, null);

        List<MemcacheCommand> expectedCommands = Arrays.asList(command1, command2);

        verify(eventBus, times(2)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), getCaptor.capture());

        List<MemcacheCommand> commands = commandCaptor.getAllValues();
        List<TranslateKeyResponseHandler> handlers = getCaptor.getAllValues();
        for (int i = 0; i < getCaptor.getAllValues().size(); i++) {
            verifyCommand(expectedCommands.get(i), commands.get(i));
            handlers.get(i).handle(Future.succeededFuture(messages.get(i)));
        }
    }

    @Test
    public void testDelete() {
        final DeleteCommandResponse response = new DeleteCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.delete("key").setHandler(new Handler<AsyncResult<DeleteCommandResponse>>() {
            @Override
            public void handle(AsyncResult<DeleteCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.delete, "namespacekey", null, null);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), deleteCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        deleteCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testTouch() {
        final TouchCommandResponse response = new TouchCommandResponse.Builder()
                .setStatus(JsendStatus.success)
                .build();

        client.touch("key", 100).setHandler(new Handler<AsyncResult<TouchCommandResponse>>() {
            @Override
            public void handle(AsyncResult<TouchCommandResponse> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.touch, "namespacekey", null, 100);

        verify(eventBus, times(1)).send(eq("address_server1"), commandCaptor.capture(), withTimeout(Long.MAX_VALUE), touchCaptor.capture());

        verifyCommand(command, commandCaptor.getValue());

        touchCaptor.getValue().handle(Future.succeededFuture(new TestMessage<>(response)));
    }

    @Test
    public void testGetNamespace() {
        assertEquals("namespace", client.getNamespace());
    }
    
    private void verifyCommand(MemcacheCommand expected, MemcacheCommand actual) {
        assertEquals(expected.getCommand(), actual.getCommand());
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getExpires(), actual.getExpires());
        assertEquals(expected.getType(), actual.getType());
    }

    private static DeliveryOptions withTimeout(final Long expectedTimeout) {
        return argThat(new DeliveryOptionsTimeoutFeatureMatcher(expectedTimeout));
    }

    private static final class DeliveryOptionsTimeoutFeatureMatcher extends FeatureMatcher<DeliveryOptions, Long> {
        DeliveryOptionsTimeoutFeatureMatcher(final Long expectedTimeout) {
            super(new Equals(expectedTimeout), "Timeout", "timeout");
        }

        @Override
        protected Long featureValueOf(final DeliveryOptions deliveryOptions) {
            return deliveryOptions.getSendTimeout();
        }
    }

}
