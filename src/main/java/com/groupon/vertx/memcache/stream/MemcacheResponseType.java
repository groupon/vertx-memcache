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
import java.io.UnsupportedEncodingException;

/**
 * List of Memcache response types.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public enum MemcacheResponseType {
    VALUE("VALUE ", false),
    STORED("STORED", true),
    DELETED("DELETED", true),
    TOUCHED("TOUCHED", true),
    NOT_STORED("NOT_STORED", true),
    EXISTS("EXISTS", true),
    NOT_FOUND("NOT_FOUND", true),
    END("END", true),
    ERROR("ERROR", true),
    CLIENT_ERROR("CLIENT ERROR", false),
    SERVER_ERROR("SERVER ERROR", false);

    public final String type;
    public final boolean exact;

    MemcacheResponseType(String type, boolean exact) {
        this.type = type;
        this.exact = exact;
    }

    public boolean matches(ByteArrayOutputStream line) {
        boolean match;
        try {
            if (!exact) {
                match = line.toString("UTF-8").startsWith(type);
            } else {
                match = line.toString("UTF-8").equals(type);
            }
        } catch (UnsupportedEncodingException e) {
            match = false;
        }
        return match;
    }
}
