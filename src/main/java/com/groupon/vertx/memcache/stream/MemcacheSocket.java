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

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.parser.LineParserType;
import com.groupon.vertx.utils.Logger;

/**
 * This sends commands to the Memcache server socket.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheSocket {
    private static final Logger log = Logger.getLogger(MemcacheSocket.class);
    private static final Charset ENCODING = Charset.forName("UTF-8");
    private final NetSocket socket;
    private final MemcacheOutputStream output;
    private final MemcacheInputStream input;
    private final ConcurrentLinkedQueue<MemcacheCommand> pendingCommands;

    public MemcacheSocket(final NetSocket socket) {
        this(socket, new ConcurrentLinkedQueue<MemcacheCommand>());
    }

    public MemcacheSocket(final NetSocket socket, ConcurrentLinkedQueue<MemcacheCommand> pendingCommands) {
        this.socket = socket;
        this.output = new MemcacheOutputStream(socket);
        this.pendingCommands = pendingCommands;
        this.input = new MemcacheInputStream(pendingCommands);

        socket.handler(buffer -> {
            try {
                input.processBuffer(buffer);
            } catch (Exception ex) {
                // Error processing the commands so close the socket.
                socket.close();
            }
        });
    }

    /**
     * This formats and writes the Memcache command to the NetSocket.  Expected output
     * to the socket is:
     * <br>
     * <code>
     * '{command name} {key} {expires} {length of value}\r\n'
     * '{value}\r\n'
     * </code>
     * <br>
     * So for the command 'SET "somekey" "blue" 300' the output would be:
     * <br>
     * <code>
     *     'SET somekey 300 4\r\nblue\r\n'
     * </code>
     *
     * @param command - Memcache command to send
     */
    public void sendCommand(MemcacheCommand command) {
        output.write(command.getCommand());
        output.writeDelim();
        output.write(command.getKey());

        if (command.getType().getLineParserType() == LineParserType.STORE) {
            output.writeDelim();
            output.write("0");
        }

        if (command.getExpires() != null) {
            output.writeDelim();
            output.write(String.valueOf(command.getExpires()));
        }

        if (command.getValue() != null) {
            output.writeDelim();
            if (command.getType().getLineParserType() == LineParserType.STORE) {
                byte[] valueBytes = command.getValue().getBytes(ENCODING);
                output.write(String.valueOf(valueBytes.length));
                output.writeCrlf();
                for (int i = 0; i < valueBytes.length; i++) {
                    output.write(valueBytes[i]);
                }
            } else {
                output.write(command.getValue());
            }
        }
        output.writeCrlf();

        pendingCommands.add(command);
        output.flush();
        log.debug("sendCommand", "commandSent", new String[]{"command", "key"}, command.getCommand(), command.getKey());
    }

    public void close() {
        MemcacheCommand command = pendingCommands.poll();
        while (command != null) {
            command.setResponse(new JsonObject("{\"status\":\"error\",\"message\":\"Socket closed unexpectedly\"}"));
            command = pendingCommands.poll();
        }

        output.close();
    }
}
