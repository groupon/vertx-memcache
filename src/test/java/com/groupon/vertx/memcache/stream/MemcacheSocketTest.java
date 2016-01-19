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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.command.MemcacheCommandType;

/**
 * Tests for <code>MemcacheSocket</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheSocketTest {
    @Mock
    private NetSocket netSocket;

    @Mock
    private MemcacheInputStream inputStream;

    @Captor
    ArgumentCaptor<Handler<Buffer>> dataCaptor;

    private MemcacheSocket memcacheSocket;
    private Field inputStreamField;
    private ConcurrentLinkedQueue<MemcacheCommand> pendingCommands;
    private Handler<Buffer> dataHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        pendingCommands = new ConcurrentLinkedQueue<>();
        memcacheSocket = new MemcacheSocket(netSocket, pendingCommands);

        inputStreamField = MemcacheSocket.class.getDeclaredField("input");
        inputStreamField.setAccessible(true);
        inputStreamField.set(memcacheSocket, inputStream);

        verify(netSocket, times(1)).handler(dataCaptor.capture());

        dataHandler = dataCaptor.getValue();
    }

    @After
    public void tearDown() {
        inputStreamField.setAccessible(false);
    }

    @Test
    public void testSocketDataHandler() {
        dataHandler.handle(Buffer.buffer());

        verify(inputStream, times(1)).processBuffer(Buffer.buffer());
    }

    @Test
    public void testSimpleGetCommand() {
        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key", null, null);

        try {
            memcacheSocket.sendCommand(command);

            assertEquals("Missing pending command", 1, pendingCommands.size());
            assertEquals("Incorrect command", command, pendingCommands.peek());
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(netSocket, times(1)).write(Buffer.buffer().appendString("get key\r\n"));
    }

    @Test
    public void testSetNoExpirationCommand() {
        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

        try {
            memcacheSocket.sendCommand(command);

            assertEquals("Missing pending command", 1, pendingCommands.size());
            assertEquals("Incorrect command", command, pendingCommands.peek());
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(netSocket, times(1)).write(Buffer.buffer().appendString("set key 0 5\r\nvalue\r\n"));
    }

    @Test
    public void testSetWithExpirationCommand() {
        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", 200);

        try {
            memcacheSocket.sendCommand(command);

            assertEquals("Missing pending command", 1, pendingCommands.size());
            assertEquals("Incorrect command", command, pendingCommands.peek());
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(netSocket, times(1)).write(Buffer.buffer().appendString("set key 0 200 5\r\nvalue\r\n"));
    }

    @Test
    public void testClose() {
        MemcacheCommand command = mock(MemcacheCommand.class);

        pendingCommands.add(command);

        memcacheSocket.close();

        verify(command, times(1)).setResponse(new JsonObject("{\"status\":\"error\",\"message\":\"Socket closed unexpectedly\"}"));
        verify(netSocket, times(1)).close();
    }
}
