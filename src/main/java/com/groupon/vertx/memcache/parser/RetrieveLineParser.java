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
package com.groupon.vertx.memcache.parser;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.client.JsendStatus;
import com.groupon.vertx.memcache.client.response.RetrieveCommandResponse;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;
import com.groupon.vertx.utils.Logger;

/**
 * This supports the parsing logic for memcache retrieval command GET.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class RetrieveLineParser extends BaseLineParser<RetrieveCommandResponse, RetrieveCommandResponse.Builder> {
    private static final Logger log = Logger.getLogger(RetrieveLineParser.class);
    private static final MemcacheResponseType[] RESPONSE_TYPES = new MemcacheResponseType[] {
        MemcacheResponseType.VALUE, MemcacheResponseType.END
    };
    private static final int VALUE_SEGMENTS = 4;
    private static final int VALUE_KEY_INDEX = 1;
    private static final int VALUE_LENGTH_INDEX = 3;

    private String expectedKey;
    private byte[] expectedBytes;
    private int bytesRetrieved = 0;

    private RetrieveCommandResponse.Builder builder;

    public RetrieveLineParser() {
        builder = new RetrieveCommandResponse.Builder();
    }

    @Override
    protected RetrieveCommandResponse.Builder getResponseBuilder() {
        return builder;
    }

    @Override
    public boolean isResponseEnd(ByteArrayOutputStream line) {
        boolean match = super.isResponseEnd(line);
        if (match) {
            return true;
        }

        MemcacheResponseType type = getResponseType(RESPONSE_TYPES, line);
        if (type != null) {
            switch (type) {
                case END:
                    builder.setStatus(JsendStatus.success);
                    match = true;
                    break;
                case VALUE:
                    parseRetrievedValue(line);
                    match = false;
                    break;
                default:
                    if (expectedBytes != null) {
                        parseRetrievedValue(line);
                        match = false;
                    } else {
                        log.error("isResponseEnd", "exception", "invalidFormat", new String[] {"line"}, getMessageNullIfError(line));
                        throw new MemcacheException("Unexpected format in response");
                    }
                    break;
            }
        } else if (expectedBytes != null) {
            parseRetrievedValue(line);
            match = false;
        } else {
            log.error("isResponseEnd", "exception", "invalidFormat", new String[] {"line"}, getMessageNullIfError(line));
            throw new MemcacheException("Unexpected format in response");
        }

        return match;
    }

    private void parseRetrievedValue(ByteArrayOutputStream line) {
        if (expectedBytes == null) {
            String valueHeader;
            try {
                valueHeader = line.toString(ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new MemcacheException("Unexpected encoding");
            }
            String[] parts = valueHeader.split(" ");
            if (parts.length < VALUE_SEGMENTS) {
                log.error("parseRetrieveValue", "exception", "invalidValueFormat", new String[] {"line"}, valueHeader);
                throw new MemcacheException("Unexpected format in response");
            }
            expectedKey = parts[VALUE_KEY_INDEX];
            expectedBytes = new byte[Integer.parseInt(parts[VALUE_LENGTH_INDEX])];
        } else if (expectedBytes.length >= (line.size() + bytesRetrieved)) {
            System.arraycopy(line.toByteArray(), 0, expectedBytes, bytesRetrieved, line.size());
            bytesRetrieved += line.size();
        } else {
            log.error("parseRetrievedValue", "exception", "invalidLength", new String[] {"length"}, line.size());
            throw new MemcacheException("Length of value exceeds expected response");
        }

        // We have finished collecting the value.
        if (bytesRetrieved == expectedBytes.length) {
            builder.addData(expectedKey, new String(expectedBytes, Charset.forName(ENCODING)));
            clearExpected();
        }
    }

    private void clearExpected() {
        expectedKey = null;
        expectedBytes = null;
        bytesRetrieved = 0;
    }
}

