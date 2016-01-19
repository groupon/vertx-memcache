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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.parser.StoreLineParser;
import com.groupon.vertx.memcache.stream.MemcacheSocket;

/**
 * Tests for <code>MemcacheCommandHandler</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheCommandHandlerTest {

    @Mock
    private NetSocket socket;

    @Mock
    private Message<JsonObject> message;

    @Mock
    private JsonObject bogusJson;

    @Mock
    private MemcacheSocket memcacheSocket;

    private JsonObject errorReply = new JsonObject("{\"status\":\"error\",\"message\":\"Invalid message with null or empty.\"}");
    private MemcacheCommandHandler handler;

    private Field tempField = null;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        tempField = MemcacheCommandHandler.class.getDeclaredField("socket");
        tempField.setAccessible(true);

        handler = new MemcacheCommandHandler(socket);
        tempField.set(handler, memcacheSocket);

        tempField.setAccessible(false);
    }

    @Test
    public void testHandleNullBody() {
        stub(message.body()).toReturn(null);

        handler.handle(message);

        verify(message, times(1)).reply(errorReply);
    }

    @Test
    public void testHandleEmptyBody() {
        stub(message.body()).toReturn(new JsonObject());

        handler.handle(message);

        verify(message, times(1)).reply(errorReply);
    }

    @Test
    public void testHandleCommand() {
        stub(message.body()).toReturn(new JsonObject("{\"command\":\"set\",\"key\":\"somekey\",\"value\":\"somevalue\",\"expires\":300}"));

        handler.handle(message);

        ArgumentCaptor<MemcacheCommand> commandCaptor = ArgumentCaptor.forClass(MemcacheCommand.class);
        verify(memcacheSocket, times(1)).sendCommand(commandCaptor.capture());

        MemcacheCommand command = commandCaptor.getValue();
        assertEquals("Invalid type", MemcacheCommandType.set, command.getType());
        assertEquals("Invalid command", "set", command.getCommand());
        assertEquals("Invalid key", "somekey", command.getKey());
        assertEquals("Invalid value", "somevalue", command.getValue());
        assertEquals("Invalid expires", 300, (int) command.getExpires());
        assertTrue("Invalid line parser", command.getLineParser() instanceof StoreLineParser);

        JsonObject testObject = new JsonObject("{\"status\":\"success\"}");
        command.setResponse(testObject);

        verify(message, times(1)).reply(testObject);
    }

    @Test
    public void testHandleCommandError() {
        stub(message.body()).toReturn(new JsonObject("{\"command\":\"set\"}"));

        handler.handle(message);

        verify(message, times(1)).reply(new JsonObject("{\"status\":\"error\",\"message\":\"Invalid command format\"}"));
    }

    @Test
    public void testFinish() {
        handler.finish();

        verify(memcacheSocket, times(1)).close();
    }

    @Test
    public void testFinishWithExceptoin() {
        doThrow(new RuntimeException()).when(memcacheSocket).close();

        try {
            handler.finish();

            verify(memcacheSocket, times(1)).close();
        } catch (Exception ex) {
            assertTrue("Unexpected exception", false);
        }
    }
}
