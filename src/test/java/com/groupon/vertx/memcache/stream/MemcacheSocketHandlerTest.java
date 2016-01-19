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


import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.server.MemcacheServer;

/**
 * Tests for <code>MemcacheSocketHandler</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheSocketHandlerTest {

    @Mock
    private Vertx vertx;

    @Mock
    private EventBus eventBus;

    @Mock
    private MemcacheServer memcacheServer;

    @Mock
    private NetClient netClient;

    @Mock
    private NetSocket netSocket;

    @Mock
    private MessageConsumer<JsonObject> consumer;

    @Captor
    private ArgumentCaptor<Handler<AsyncResult<NetSocket>>> connectCaptor;

    @Captor
    private ArgumentCaptor<Handler<Message<Object>>> registerCaptor;

    @Captor
    private ArgumentCaptor<Handler<Throwable>> exceptionCaptor;

    @Captor
    private ArgumentCaptor<Handler<Void>> closeCaptor;

    @Captor
    private ArgumentCaptor<Handler<Long>> timerCaptor;

    private Handler<AsyncResult<NetSocket>> asyncResultHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        stub(memcacheServer.getPort()).toReturn(11211);
        stub(memcacheServer.getServer()).toReturn("host:11211");
        stub(memcacheServer.getHost()).toReturn("host");
        stub(vertx.eventBus()).toReturn(eventBus);
        doReturn(consumer).when(eventBus).consumer(anyString(), Mockito.<Handler<Message<Object>>>any());

        MemcacheSocketHandler handler = new MemcacheSocketHandler(vertx, "address", memcacheServer, netClient, 1);

        handler.handle(1L);

        verify(netClient, times(1)).connect(eq(11211), eq("host"), connectCaptor.capture());

        asyncResultHandler = connectCaptor.getValue();
    }

    @Test
    public void testSocketSucceeded() {
        asyncResultHandler.handle(Future.succeededFuture(netSocket));

        verify(netSocket, times(1)).exceptionHandler(exceptionCaptor.capture());
        verify(netSocket, times(1)).exceptionHandler(exceptionCaptor.capture());

        verify(eventBus, times(1)).consumer(eq("address"), registerCaptor.capture());
    }

    @Test
    public void testSocketSuccessFollowedByException() {
        asyncResultHandler.handle(Future.succeededFuture(netSocket));

        verify(netSocket, times(1)).exceptionHandler(exceptionCaptor.capture());
        verify(eventBus, times(1)).consumer(eq("address"), registerCaptor.capture());

        Handler<Throwable> exceptionHandler = exceptionCaptor.getValue();
        exceptionHandler.handle(new Exception("Failed"));

        verify(consumer, times(1)).unregister();
        verify(netSocket, times(1)).close();
    }

    @Test
    public void testSocketSuccessFollowedByClose() {
        asyncResultHandler.handle(Future.succeededFuture(netSocket));

        verify(netSocket, times(1)).closeHandler(closeCaptor.capture());
        verify(eventBus, times(1)).consumer(eq("address"), registerCaptor.capture());

        closeCaptor.getValue().handle(null);

        verify(consumer, times(1)).unregister();
        verify(netSocket, times(1)).close();
    }

    @Test
    public void testSocketFailed() {
        asyncResultHandler.handle(Future.failedFuture(new Exception("Failed")));

        verify(vertx, times(1)).setTimer(eq(2L), timerCaptor.capture());
    }

    @Test
    public void testSocketFailedMultipleTimes() {
        asyncResultHandler.handle(Future.failedFuture(new Exception("Failed")));

        verify(vertx, times(1)).setTimer(eq(2L), timerCaptor.capture());

        asyncResultHandler.handle(Future.failedFuture(new Exception("Failed")));

        verify(vertx, times(1)).setTimer(eq(4L), timerCaptor.capture());

        asyncResultHandler.handle(Future.failedFuture(new Exception("Failed")));

        verify(vertx, times(1)).setTimer(eq(8L), timerCaptor.capture());
    }

    @Test
    public void testSocketHitsMaxTimeout() {
        for (int i = 1; i < 16; i++) {
            asyncResultHandler.handle(Future.failedFuture(new Exception("Failed")));

            verify(vertx, times(1)).setTimer(eq((long) Math.pow(2, i)), timerCaptor.capture());
        }

        asyncResultHandler.handle(Future.failedFuture(new Exception("Failed")));

        verify(vertx, times(1)).setTimer(eq(60000L), timerCaptor.capture());
    }
}
