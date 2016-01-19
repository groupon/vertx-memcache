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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for <code>MemcacheOutputStream</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheOutputStreamTest {
    @Mock
    private NetSocket socket;

    private MemcacheOutputStream output = null;
    private Field buffer = null;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        buffer = MemcacheOutputStream.class.getDeclaredField("buffer");
        buffer.setAccessible(true);

        output = new MemcacheOutputStream(socket, 5);
    }

    @After
    public void tearDown() throws Exception {
        buffer.setAccessible(false);
    }

    @Test
    public void testWriteInsideBuffer() {
        try {
            output.write("awes");

            assertEquals("Unexpected buffer size", 4, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("awes"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testWriteOutsideBuffer() {
        Buffer buff = Buffer.buffer();
        buff.appendString("aweso");

        try {
            output.write("awesome");

            assertEquals("Unexpected buffer size", 2, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("me"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, times(1)).write(buff);
    }

    @Test
    public void testWriteCrlfInsideBuffer() {
        output.writeCrlf();

        try {
            assertEquals("Unexpected buffer size", 2, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("\r\n"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testWriteCrlfOutsideBuffer() {
        try {
            output.write("some");
            output.writeCrlf();

            assertEquals("Unexpected buffer size", 2, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("\r\n"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, times(1)).write(Buffer.buffer().appendString("some"));
    }

    @Test
    public void testFlushBuffer() {
        try {
            output.write("Hi");
            output.flush();

            assertEquals("Unexpected buffer size", 0, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer(), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, times(1)).write(Buffer.buffer().appendString("Hi"));
    }

    @Test
    public void testWriteIntegerInsideBuffer() {
        output.write(1234);

        try {
            assertEquals("Unexpected buffer size", 4, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("1234"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testWriteIntegerOutsideBuffer() {
        try {
            output.write("foo");
            output.write(1234);

            assertEquals("Unexpected buffer size", 4, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("1234"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, times(1)).write(Buffer.buffer().appendString("foo"));
    }

    @Test
    public void testWriteByteInsideBuffer() {
        output.write((byte) 'a');

        try {
            assertEquals("Unexpected buffer size", 1, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer().appendString("a"), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testWriteByteOutsideBuffer() {
        output.write((byte) 'a');
        output.write((byte) 'b');
        output.write((byte) 'c');
        output.write((byte) 'd');
        output.write((byte) 'e');

        try {
            assertEquals("Unexpected buffer size", 0, ((Buffer) buffer.get(output)).length());
            assertEquals("Buffer doesn't match", Buffer.buffer(), buffer.get(output));
        } catch (Exception ex) {
            assertNull("Unexpected exception", ex);
        }

        verify(socket, times(1)).write(Buffer.buffer().appendString("abcde"));
    }
}
