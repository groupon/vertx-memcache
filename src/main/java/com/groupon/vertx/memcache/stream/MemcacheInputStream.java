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

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.memcache.command.MemcacheCommand;
import com.groupon.vertx.memcache.parser.LineParser;
import com.groupon.vertx.utils.Logger;

/**
 * The following code was based off of code from the Jedis library which can be found here:
 * <br>
 * https://github.com/xetorthio/jedis
 * <br>
 * The license is below:
 * <br>
 * Copyright (c) 2011 Jonathan Leibiusky
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * <br>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheInputStream {
    private static final Logger log = Logger.getLogger(MemcacheInputStream.class);
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final ConcurrentLinkedQueue<MemcacheCommand> pendingCommands;
    private ByteArrayOutputStream buffer;
    private byte previous;

    /**
     * create a MemcacheInputStream parser that will process the commands reading the buffer received
     *
     * @param pendingCommands the commands
     */
    public MemcacheInputStream(ConcurrentLinkedQueue<MemcacheCommand> pendingCommands) {
        this.pendingCommands = pendingCommands;
        this.buffer = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
        this.previous = 0;
    }

    /**
     * create a MemcacheInputStream parser that will process the commands reading the buffer received
     *
     * @param pendingCommands the commands
     * @param bufferSize      size of the buffer to read the response
     */
    @Deprecated
    public MemcacheInputStream(ConcurrentLinkedQueue<MemcacheCommand> pendingCommands, int bufferSize) {
        this(pendingCommands);
    }

    /**
     * This method handles processing the incoming Buffer from the NetSocket.  The Buffer
     * is not guaranteed to contain a whole message so this method tracks the current state
     * of the incoming data and notifies the pending commands when enough data has been sent
     * for a response.
     *
     * @param processBuffer - The Buffer containing the current set of bytes.
     */
    public void processBuffer(Buffer processBuffer) {
        if (processBuffer == null || processBuffer.length() == 0) {
            return;
        }

        byte first;
        byte second;

        ByteBuf byteBuf = processBuffer.getByteBuf();

        while (byteBuf.isReadable()) {
            first = byteBuf.readByte();
            if (first == '\r') {
                if (byteBuf.isReadable()) {
                    second = byteBuf.readByte();
                    if (second == '\n') {
                        addCompletedLine();
                    }
                    previous = second;
                } else {
                    previous = first;
                }
            } else if (first == '\n' && previous == '\r') {
                addCompletedLine();
                previous = first;
            } else {
                buffer.write(first);
                previous = first;
            }
        }
    }

    /**
     * This method is fired when enough data is in the Buffer to complete a command.  If the
     * command does not match the signature of the buffered data then an exception is thrown
     * and the socket should be closed as the command/response queues are no longer in sync.
     *
     * @param command - The command to process from the response buffer.
     */
    private void processCommand(MemcacheCommand command) {
        if (command == null) {
            // No command to process so return.  Should add log message here.
            log.warn("processCommand", "noCommandFound");
            return;
        }

        LineParser parser = command.getLineParser();
        JsonObject response = parser.getResponse();

        log.trace("processCommand", "redisCommandSuccess", new String[]{"command", "data"}, command.getCommand(),
                response.getValue("data"));

        command.setResponse(response);
    }

    /**
     * When the crlf sequence has been received from the Buffer it is time to check if we
     * have enough data to complete a command and clear the line off of the current buffer.
     *
     */
    protected void addCompletedLine() {
        previous = 0;
        if (pendingCommands.size() > 0) {
            MemcacheCommand command = pendingCommands.peek();
            LineParser parser = command.getLineParser();
            if (parser.isResponseEnd(buffer)) {
                processCommand(pendingCommands.poll());
            }
        } else {
            log.warn("addCompletedLine", "noPendingCommands");
        }
        buffer = new ByteArrayOutputStream();
    }
}
