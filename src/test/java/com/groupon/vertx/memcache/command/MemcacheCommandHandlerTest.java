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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import io.vertx.core.net.NetSocket;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.vertx.memcache.TestMessage;
import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.MemcacheCommandResponse;
import com.groupon.vertx.memcache.client.response.StoreCommandResponse;
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
    private MemcacheSocket memcacheSocket;

    private TestMessage<MemcacheCommand> message;

    private MemcacheCommandResponse errorReply = new MemcacheCommandResponse.Builder()
            .setStatus(JsendStatus.error)
            .setMessage("Invalid message with null or empty.")
            .build();

    private MemcacheCommandHandler handler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Field tempField = MemcacheCommandHandler.class.getDeclaredField("socket");
        tempField.setAccessible(true);

        handler = new MemcacheCommandHandler(socket);
        tempField.set(handler, memcacheSocket);

        tempField.setAccessible(false);
    }

    @Test
    public void testHandleNullBody() {
        message = new TestMessage<>(null);
        stub(message.body()).toReturn(null);

        handler.handle(message);

        MemcacheCommandResponse response = (MemcacheCommandResponse) message.getReply();
        assertNotNull(response);
        assertEquals(response.getStatus(), errorReply.getStatus());
        assertEquals(response.getMessage(), errorReply.getMessage());
    }

    @Test
    public void testHandleCommand() {
        message = new TestMessage<>(
                new MemcacheCommand(MemcacheCommandType.set, "somekey", "somevalue", 300)
        );

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

        StoreCommandResponse response = new StoreCommandResponse.Builder().setStatus(JsendStatus.success).build();
        command.setResponse(response);

        StoreCommandResponse reply = (StoreCommandResponse) message.getReply();
        assertNotNull(reply);
        assertEquals(reply.getStatus(), response.getStatus());
        assertEquals(reply.getData(), response.getData());
    }

    @Test
    public void testHandleCommandError() {
        message = new TestMessage<>(new MemcacheCommand(MemcacheCommandType.set, "key", "value", null));

        handler.handle(message);

        ArgumentCaptor<MemcacheCommand> commandCaptor = ArgumentCaptor.forClass(MemcacheCommand.class);
        verify(memcacheSocket, times(1)).sendCommand(commandCaptor.capture());

        StoreCommandResponse response = new StoreCommandResponse.Builder()
                .setStatus(JsendStatus.error)
                .setMessage("Invalid command format")
                .build();
        commandCaptor.getValue().setResponse(response);

        MemcacheCommandResponse reply = (MemcacheCommandResponse) message.getReply();
        assertNotNull(reply);
        assertEquals(reply.getStatus(), response.getStatus());
        assertEquals(reply.getMessage(), response.getMessage());
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
