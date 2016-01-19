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
package com.groupon.vertx.memcache.client;

/**
 * This is an exception for capturing errors from the memcache processing.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.1.0
 */
public class MemcacheClientException extends Exception {
    private static final long serialVersionUID = -8199950128631813824L;

    public MemcacheClientException(String message) {
        super(message);
    }
}
