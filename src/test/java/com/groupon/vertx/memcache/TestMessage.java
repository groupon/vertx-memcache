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
package com.groupon.vertx.memcache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

/**
 * Generic Message implementation for testing.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 3.1.0
 */
public class TestMessage<T> implements Message<T> {
    private String address;
    private T body;
    private Object reply;


    public TestMessage(T body) {
        this(null, body);
    }

    public TestMessage(String address, T body) {
        this.address = address;
        this.body = body;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public MultiMap headers() {
        return null;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public String replyAddress() {
        return address;
    }

    @Override
    public boolean isSend() {
        return false;
    }

    @Override
    public void reply(Object value) {
        this.reply = value;
    }

    @Override
    public <R> void reply(Object o, Handler<AsyncResult<Message<R>>> handler) {
        this.reply = o;
    }

    @Override
    public void reply(Object o, DeliveryOptions deliveryOptions) {
        this.reply = o;
    }

    @Override
    public <R> void reply(Object o, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<R>>> handler) {
        this.reply = o;
    }

    public Object getReply() {
        return reply;
    }

    @Override
    public void fail(int i, String s) {
    }
}
