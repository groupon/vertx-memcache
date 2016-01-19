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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for <code>MemcacheClusterVerticle</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheClusterVerticleTest {
    @Mock
    private Vertx vertx;

    @Mock
    private Context context;

    @Mock
    private EventBus eventBus;

    @Mock
    private NetClient netClient;

    @Mock
    private Future<Void> startFuture;

    @Mock
    private AsyncResult<NetSocket> socket;

    @Mock
    private NetSocket netSocket;

    @Captor
    private ArgumentCaptor<Handler<AsyncResult<NetSocket>>> socketCaptor;

    @Captor
    private ArgumentCaptor<Handler<Message<Object>>> registerCaptor;

    private MemcacheClusterVerticle verticle;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        stub(vertx.eventBus()).toReturn(eventBus);
        stub(vertx.createNetClient()).toReturn(netClient);
        doReturn(context).when(vertx).getOrCreateContext();

        verticle = new MemcacheClusterVerticle();
        verticle.init(vertx, context);
    }

    @After
    public void tearDown() throws Exception {
        if (verticle != null) {
            verticle.stop();
        }
    }

    @Test
    public void testStartValidConfig() {
        JsonObject config = new JsonObject("{\"memcacheClusterConfig\":{\"eventBusAddressPrefix\":\"address\",\"clusters\":{\"a\":{\"servers\":[\"server\"]}}}}");

        stub(context.config()).toReturn(config);

        verticle.start(startFuture);

        verify(context, times(1)).config();
        verify(vertx, times(1)).createNetClient();

        verify(netClient, times(1)).connect(Matchers.eq(11211), Matchers.eq("server"), socketCaptor.capture());
        Handler<AsyncResult<NetSocket>> socketHandler = socketCaptor.getValue();

        stub(socket.succeeded()).toReturn(true);
        stub(socket.result()).toReturn(netSocket);

        socketHandler.handle(socket);

        verify(eventBus, times(1)).consumer(Matchers.eq("address_server"), registerCaptor.capture());
    }

    @Test
    public void testStartInvalidConfig() {
        JsonObject config = new JsonObject("{\"memcacheClusterConfig\":{\"clusters\":{\"servers\":[\"foo\"]}}}");

        stub(context.config()).toReturn(config);

        verticle.start(startFuture);

        verify(context, times(1)).config();
        verify(vertx, never()).createNetClient();
        verify(startFuture, times(1)).fail(any(Exception.class));
    }

    @Test
    public void testStartMissingConfig() {
        JsonObject config = new JsonObject("{}");

        stub(context.config()).toReturn(config);

        verticle.start(startFuture);

        verify(context, times(1)).config();
        verify(vertx, never()).createNetClient();
        verify(startFuture, times(1)).fail(any(Exception.class));
    }
}
