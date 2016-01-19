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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

/**
 * The following code was modified from the Jedis library which can be found here:
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
public class MemcacheOutputStream {
    private static final int[] SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    private static final Charset ENCODING = Charset.forName("UTF-8");
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int WRITE_QUEUE_MAX_SIZE = 123;
    private static final byte[] DIGIT_TENS = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };
    private static final byte[] DIGIT_ONES = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };
    private static final byte[] DIGITS = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };
    private final NetSocket socket;
    private final int maxBuffer;
    private Buffer buffer;

    public MemcacheOutputStream(NetSocket socket) {
        this(socket, DEFAULT_BUFFER_SIZE);
    }

    public MemcacheOutputStream(NetSocket socket, final int maxBuffer) {
        if (maxBuffer <= 0) {
            throw new IllegalArgumentException("Invalid buffer size");
        }

        this.socket = socket;
        this.buffer = Buffer.buffer();
        this.maxBuffer = maxBuffer;
    }

    public void write(final byte b) {
        buffer.appendByte(b);
        if (buffer.length() == maxBuffer) {
            flushBuffer();
        }
    }

    public void write(String in) {
        byte[] bytes = in.getBytes(ENCODING);
        for (int i = 0; i < bytes.length; i++) {
            write(bytes[i]);
        }
    }

    public void write(int value) {
        if (value < 0) {
            write('-');
            value = -value;
        }

        int size = 0;
        while (value > SIZE_TABLE[size]) {
            size++;
        }

        size++;
        if (size >= maxBuffer - buffer.length()) {
            flushBuffer();
        }

        int q;
        int r;
        int charPos = buffer.length() + size;

        // CS.OFF: MagicNumber
        while (value >= 65536) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer.setByte(--charPos, DIGIT_ONES[r]);
            buffer.setByte(--charPos, DIGIT_TENS[r]);
        }

        for (;;) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));
            buffer.setByte(--charPos, DIGITS[r]);
            value = q;
            if (value == 0) {
                break;
            }
        }
        // CS.ON: MagicNumber
    }

    public void writeDelim() {
        buffer.appendByte((byte) ' ');
    }

    public void writeCrlf() {
        if (2 >= (maxBuffer - buffer.length())) {
            flushBuffer();
        }

        buffer.appendByte((byte) '\r');
        buffer.appendByte((byte) '\n');
    }

    public void flush() {
        flushBuffer();
        socket.setWriteQueueMaxSize(WRITE_QUEUE_MAX_SIZE);
    }

    public void close() {
        flush();
        socket.close();
    }

    private void flushBuffer() {
        if (buffer.length() > 0) {
            socket.write(buffer);
            buffer = Buffer.buffer();
        }
    }
}
