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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    private Field bufferPosition = null;

    @Before
    public void setUp() throws Exception {
        pendingCommands = new ConcurrentLinkedQueue<>();

        bufferPosition = MemcacheInputStream.class.getDeclaredField("bufferPosition");
        bufferPosition.setAccessible(true);
    }

    @After
    public void tearDown() throws Exception {
        pendingCommands.clear();
        pendingCommands = null;

        bufferPosition.setAccessible(false);
    }

    @Test
    public void testProcessEmptyBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            input.processBuffer(Buffer.buffer());

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessNullBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            input.processBuffer(null);

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessSingleByteBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            Buffer buff = Buffer.buffer();
            buff.appendByte((byte) 'a');
            input.processBuffer(buff);

            assertEquals("Invalid buffer position", 1, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessTouchBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.touch, "key", null, null);

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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessClientErrorBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessServerErrorBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessErrorBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.set, "key", "value", null);

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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessDeleteBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.delete, "key", null, null);

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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessModifyNotFoundBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.incr, "key", "1", null);

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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessModifyBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessRetrieveBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessRetrieveMultipleBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
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

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessRetrieveMultipleUnfinishedBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.get, "key key1", null, null);

            command.commandResponseHandler(new Handler<JsonObject>() {
                public void handle(JsonObject command) {
                    assertFalse("Command response handler called unexpectedly", true);
                }
            });

            pendingCommands.add(command);

            Buffer buff = Buffer.buffer();
            buff.appendString("VALUE key 0 3\r\nfoo\r\nVALUE key1 0 3\r\nbar\r\n");
            input.processBuffer(buff);

            assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
        } catch (Exception ex) {
            assertNull("Unexpected exception: " + ex.getMessage(), ex);
        }
    }

    @Test
    public void testProcessInvalidBuffer() {
        MemcacheInputStream input = new MemcacheInputStream(pendingCommands);

        try {
            MemcacheCommand command = new MemcacheCommand(MemcacheCommandType.touch, "key", null, null);

            command.commandResponseHandler(new Handler<JsonObject>() {
                public void handle(JsonObject command) {
                    assertFalse("Command response handler called unexpectedly", true);
                }
            });

            pendingCommands.add(command);

            Buffer buff = Buffer.buffer();
            buff.appendString("END\r\n");
            input.processBuffer(buff);

            assertFalse("Exception did not occur", true);
        } catch (Exception ex) {
            try {
                assertEquals("Invalid buffer position", 0, bufferPosition.getInt(input));
            } catch (Exception exc) {
                assertNull("Unexpected runtime exception", exc);
            }
        }
    }
}
