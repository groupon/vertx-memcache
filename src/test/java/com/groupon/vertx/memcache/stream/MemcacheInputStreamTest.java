/**
 * Copyright 2014 Groupon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.memcache.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.command.MemcacheCommandType;

/**
 * Tests for <code>MemcacheInputStream</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheInputStreamTest {
    private ConcurrentLinkedQueue<MemcacheCommand> pendingCommands = null;

    ArgumentCaptor<byte[]> captor;

    @Before
    public void setUp() throws Exception {
        pendingCommands = new ConcurrentLinkedQueue<>();
        captor = ArgumentCaptor.forClass(byte[].class);
    }

    @After
    public void tearDown() throws Exception {
        pendingCommands.clear();
    }

    @Test
    public void testProcessEmptyBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));
        input.processBuffer(Buffer.buffer());
        verify(input, never()).addCompletedLine(any(byte[].class));
    }

    @Test
    public void testProcessNullBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));
        input.processBuffer(null);
        verify(input, never()).addCompletedLine(any(byte[].class));
    }

    @Test
    public void testProcessSingleByteBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        Buffer buff = Buffer.buffer();
        buff.appendByte((byte) 'a');
        input.processBuffer(buff);
        Field bufferField = MemcacheInputStream.class.getDeclaredField("buffer");
        bufferField.setAccessible(true);
        ByteArrayOutputStream buffer = (ByteArrayOutputStream) bufferField.get(input);
        String s = new String(buffer.toByteArray());
        assertEquals("a", s);
        bufferField.setAccessible(false);
    }

    @Test
    public void testProcessBigSingleByteBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        Buffer buff = Buffer.buffer();
        for (int i = 0; i < 8194; i++) {
            buff.appendByte((byte) 'a');
        }
        input.processBuffer(buff);
        Field bufferField = MemcacheInputStream.class.getDeclaredField("buffer");
        bufferField.setAccessible(true);
        ByteArrayOutputStream buffer = (ByteArrayOutputStream) bufferField.get(input);
        assertEquals("Invalid buffer size", 8194, buffer.size());
        bufferField.setAccessible(false);
    }

    @Test
    public void testProcessTouchBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.touch, "key", null, null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", MemcacheResponseType.TOUCHED.name(), command.getString("data"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("TOUCHED\r\n");
        input.processBuffer(buff);
        List<byte[]> values = captor.getAllValues();
        assertEquals("TOUCHED", new String(values.get(0)));
    }

    @Test
    public void testProcessClientErrorBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "error", command.getString("status"));
                assertEquals("Invalid message", "CLIENT ERROR message", command.getString("message"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("CLIENT ERROR message\r\n");
        input.processBuffer(buff);
        List<byte[]> values = captor.getAllValues();
        assertEquals("CLIENT ERROR message", new String(values.get(0)));
    }

    @Test
    public void testProcessServerErrorBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "error", command.getString("status"));
                assertEquals("Invalid message", "SERVER ERROR message", command.getString("message"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("SERVER ERROR message\r\n");
        input.processBuffer(buff);
        List<byte[]> values = captor.getAllValues();
        assertEquals("SERVER ERROR message", new String(values.get(0)));
    }

    @Test
    public void testProcessErrorBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "error", command.getString("status"));
                assertEquals("Invalid message", "ERROR", command.getString("message"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("ERROR\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("ERROR", new String(values.get(0)));
    }

    @Test
    public void testProcessDeleteBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.delete, "key", null, null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", "DELETED", command.getString("data"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("DELETED\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("DELETED", new String(values.get(0)));
    }

    @Test
    public void testProcessModifyNotFoundBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.incr, "key", "1", null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", null, command.getString("data"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("NOT_FOUND\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("NOT_FOUND", new String(values.get(0)));
    }

    @Test
    public void testProcessModifyBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.incr, "key", "1", null);

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", 2, command.getLong("data").intValue());
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("2\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("2", new String(values.get(0)));
    }

    @Test
    public void testProcessRetrieveBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key", null, null);

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", new JsonObject("{\"key\": \"foobar\"}"), command.getJsonObject("data"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("VALUE key 0 6\r\nfoobar\r\nEND\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("VALUE key 0 6", new String(values.get(0)));
        assertEquals("foobar", new String(values.get(1)));
        assertEquals("END", new String(values.get(2)));
    }

    @Test
    public void testProcessRetrieveMultipleBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key key1", null, null);

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));

                JsonObject data = command.getJsonObject("data");
                assertNotNull("Missing data", data);
                assertEquals("Wrong number of results", 2, data.size());
                assertEquals("Invalid data", "foo", data.getString("key"));
                assertEquals("Invalid data", "bar", data.getString("key1"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("VALUE key 0 3\r\nfoo\r\nVALUE key1 0 3\r\nbar\r\nEND\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("VALUE key 0 3", new String(values.get(0)));
        assertEquals("foo", new String(values.get(1)));
        assertEquals("VALUE key1 0 3", new String(values.get(2)));
        assertEquals("bar", new String(values.get(3)));
        assertEquals("END", new String(values.get(4)));
    }

    @Test
    public void testProcessRetrieveMultipleUnfinishedBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key key1", null, null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertFalse("Command response handler called unexpectedly", true);
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("VALUE key 0 3\r\nfoo\r\nVALUE key1 0 3\r\nbar\r\n");
        input.processBuffer(buff);
        List<byte[]> values = captor.getAllValues();
        assertEquals("VALUE key 0 3", new String(values.get(0)));
        assertEquals("foo", new String(values.get(1)));
        assertEquals("VALUE key1 0 3", new String(values.get(2)));
        assertEquals("bar", new String(values.get(3)));
    }

    @Test(expected = MemcacheException.class)
    public void testProcessInvalidBuffer() throws Exception {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.touch, "key", null, null);

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertFalse("Command response handler called unexpectedly", true);
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("END\r\n");
        input.processBuffer(buff);
    }

    @Test
    public void testTwoBuffersEndLine() {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key", null, null);

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", new JsonObject("{\"key\": \"foobar\"}"), command.getJsonObject("data"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("VALUE key 0 6\r");
        input.processBuffer(buff);
        buff = Buffer.buffer();
        buff.appendString("\nfoobar\r\nEND\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("VALUE key 0 6", new String(values.get(0)));
        assertEquals("foobar", new String(values.get(1)));
        assertEquals("END", new String(values.get(2)));
    }

    @Test
    public void testTwoBuffersEndLineNext() {
        MemcacheInputStream input = Mockito.spy(new MemcacheInputStream(pendingCommands));

        doCallRealMethod().when(input).addCompletedLine(captor.capture());

        MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key", null, null);

        command.commandResponseHandler(new Handler<JsonObject>() {
            public void handle(JsonObject command) {
                assertNotNull("Invalid command json response", command);
                assertEquals("Invalid status", "success", command.getString("status"));
                assertEquals("Invalid data", new JsonObject("{\"key\": \"foobar\"}"), command.getJsonObject("data"));
            }
        });

        pendingCommands.add(command);

        Buffer buff = Buffer.buffer();
        buff.appendString("VALUE key 0 6");
        input.processBuffer(buff);
        buff = Buffer.buffer();
        buff.appendString("\r\nfoobar\r\nEND\r\n");
        input.processBuffer(buff);

        List<byte[]> values = captor.getAllValues();
        assertEquals("VALUE key 0 6", new String(values.get(0)));
        assertEquals("foobar", new String(values.get(1)));
        assertEquals("END", new String(values.get(2)));
    }
}
