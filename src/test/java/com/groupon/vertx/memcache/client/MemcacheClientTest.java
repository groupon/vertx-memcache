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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hamcrest.FeatureMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Equals;

import com.groupon.vertx.memcache.MemcacheConfig;
import com.groupon.vertx.memcache.MemcacheKeys;
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

    @Mock
    private Message<JsonObject> message;

    @Mock
    private Message<JsonObject> message2;

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
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.incr("key", 1).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"incr\",\"key\":\"namespacekey\",\"value\":\"1\"}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testDecr() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.decr("key", 1).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"decr\",\"key\":\"namespacekey\",\"value\":\"1\"}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testSet() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.set("key", "value", 100).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"set\",\"key\":\"namespacekey\",\"value\":\"value\",\"expires\":100}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testAdd() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.add("key", "value", 100).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"add\",\"key\":\"namespacekey\",\"value\":\"value\",\"expires\":100}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testReplace() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.replace("key", "value", 100).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"replace\",\"key\":\"namespacekey\",\"value\":\"value\",\"expires\":100}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testAppend() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.append("key", "value").setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"append\",\"key\":\"namespacekey\",\"value\":\"value\"}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testPrepend() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.prepend("key", "value").setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"prepend\",\"key\":\"namespacekey\",\"value\":\"value\"}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testGet() {
        JsonObject rawResponse = new JsonObject("{\"status\":\"success\",\"data\":{\"namespacekey\":\"value\"}}");
        stub(message.body()).toReturn(rawResponse);

        final JsonObject response = new JsonObject("{\"status\":\"success\",\"data\":{\"key\":\"value\"}}");
        client.get("key").setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"get\",\"key\":\"namespacekey\"}");

        ArgumentCaptor<TranslateKeyResponseHandler> eventCaptor = ArgumentCaptor.forClass(TranslateKeyResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testMultiGet() {
        JsonObject getresponse1 = new JsonObject("{\"status\":\"success\",\"data\":{\"namespacekey1\":\"value\"}}");
        JsonObject getresponse2 = new JsonObject("{\"status\":\"success\",\"data\":{\"namespacekey2\":\"value\"}}");

        doReturn(getresponse1).when(message).body();
        doReturn(getresponse2).when(message2).body();

        List<Message<JsonObject>> messages = Arrays.asList(message, message2);

        final JsonObject response = new JsonObject("{\"status\":\"success\",\"data\":{\"key1\":\"value\",\"key2\":\"value\"}}");
        client.get(Arrays.asList("key1", "key2")).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"get\",\"key\":\"namespacekey1\"}");

        ArgumentCaptor<TranslateKeyResponseHandler> eventCaptor = ArgumentCaptor.forClass(TranslateKeyResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand.put("key", "namespacekey2")), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        List<TranslateKeyResponseHandler> handlers = eventCaptor.getAllValues();
        for (int i = 0; i < eventCaptor.getAllValues().size(); i++) {
            handlers.get(i).handle(Future.succeededFuture(messages.get(i)));
        }
    }

    @Test
    public void testMultiGetWithMissingData() {
        JsonObject getresponse1 = new JsonObject("{\"status\":\"success\",\"data\":{\"namespacekey1\":\"value\"}}");
        JsonObject getresponse2 = new JsonObject("{\"status\":\"success\"}}");

        doReturn(getresponse1).when(message).body();
        doReturn(getresponse2).when(message2).body();

        List<Message<JsonObject>> messages = Arrays.asList(message, message2);

        final JsonObject response = new JsonObject("{\"status\":\"success\",\"data\":{\"key1\":\"value\"}}");
        client.get(Arrays.asList("key1", "key2")).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"get\",\"key\":\"namespacekey1\"}");

        ArgumentCaptor<TranslateKeyResponseHandler> eventCaptor = ArgumentCaptor.forClass(TranslateKeyResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand.put("key", "namespacekey2")), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        List<TranslateKeyResponseHandler> handlers = eventCaptor.getAllValues();
        for (int i = 0; i < eventCaptor.getAllValues().size(); i++) {
            handlers.get(i).handle(Future.succeededFuture(messages.get(i)));
        }
    }

    @Test
    public void testDelete() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.delete("key").setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"delete\",\"key\":\"namespacekey\"}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testTouch() {
        final JsonObject response = new JsonObject("{\"status\":\"success\"}");
        stub(message.body()).toReturn(response);

        client.touch("key", 100).setHandler(new Handler<AsyncResult<JsonObject>>() {
            public void handle(AsyncResult<JsonObject> result) {
                assertEquals("Result doesn't match", response, result.result());
            }
        });

        JsonObject jsonCommand = new JsonObject("{\"command\":\"touch\",\"key\":\"namespacekey\",\"expires\":100}");

        ArgumentCaptor<MemcacheClientResponseHandler> eventCaptor = ArgumentCaptor.forClass(MemcacheClientResponseHandler.class);
        verify(eventBus, times(1)).send(eq("address_server1"), eq(jsonCommand), withTimeout(Long.MAX_VALUE), eventCaptor.capture());

        eventCaptor.getValue().handle(Future.succeededFuture(message));
    }

    @Test
    public void testGetNamespace() {
        assertEquals("namespace", client.getNamespace());
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
