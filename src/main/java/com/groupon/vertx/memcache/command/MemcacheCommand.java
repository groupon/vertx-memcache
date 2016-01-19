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
package com.groupon.vertx.memcache.command;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.parser.DeleteLineParser;
import com.groupon.vertx.memcache.parser.LineParser;
import com.groupon.vertx.memcache.parser.LineParserType;
import com.groupon.vertx.memcache.parser.ModifyLineParser;
import com.groupon.vertx.memcache.parser.RetrieveLineParser;
import com.groupon.vertx.memcache.parser.StoreLineParser;
import com.groupon.vertx.memcache.parser.TouchLineParser;
import com.groupon.vertx.utils.Logger;

/**
 * This class encapsulates the information necessary to send a command to the
 * Memcache server.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class MemcacheCommand {
    private static final Logger log = Logger.getLogger(MemcacheCommand.class);
    private MemcacheCommandType type;
    private String key;
    private String value;
    private Integer expires;
    private LineParser parser;
    private Handler<JsonObject> commandResponseHandler;

    /**
     * If the command represented by the JsonObject doesn't come in the form:
     * <br>
     * <code>
     * {
     *   'command': 'SET',
     *   'key': 'somekey',
     *   'value': 'somevalue',
     *   'expires': 300
     * }
     * </code>
     * <br>
     * Then an exception will be thrown.
     *
     * @param commandJson - The command to be created.
     */
    public MemcacheCommand(JsonObject commandJson) {
        if (commandJson == null || commandJson.size() < 2) {
            log.warn("initMemcacheCommand", "failure", new String[]{"reason"}, "Invalid command format");
            throw new IllegalArgumentException("Invalid command format");
        }

        try {
            type = MemcacheCommandType.valueOf(commandJson.getString("command"));
        } catch (Exception ex) {
            log.warn("initMemcacheCommand", "failure", new String[]{"reason"}, "Invalid command");
            throw new IllegalArgumentException("Invalid or unsupported command provided");
        }

        setLineParser(type.getLineParserType());

        key = commandJson.getString("key");
        value = commandJson.getString("value");
        expires = commandJson.getInteger("expires");
    }

    /**
     * A helper method for manually building a command.
     *
     * @param type - An enum for the command type.
     * @param key - A String containing the key.
     * @param value - The String value to be sent using the command.
     * @param expires - An integer with the seconds before expiration.
     */
    public MemcacheCommand(MemcacheCommandType type, String key, String value, Integer expires) {
        if (type == null || key == null) {
            log.warn("initMemcacheCommand", "failure", new String[]{"reason"}, "Invalid command format");
            throw new IllegalArgumentException("Invalid command format");
        }

        setLineParser(type.getLineParserType());

        this.type = type;
        this.key = key;
        this.value = value;
        this.expires = expires;
    }

    /**
     * The MemcacheCommandType enum which represents the command type being sent to Memcache.
     *
     * @return - A MemcacheCommandType representing the current command.
     */
    public MemcacheCommandType getType() {
        return type;
    }

    /**
     * The String name for the command.
     *
     * @return - A String containing the name for the Memcache command.
     */
    public String getCommand() {
        return type.getCommand();
    }

    /**
     * The key for inserting, updating, or deleting an object in the store.
     *
     * @return - A String containing the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * The value to be stored or updated in the store.
     *
     * @return - A String containing the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * The number of seconds for updating the expiration of a key.
     *
     * @return - An integer representing the number of seconds before a key expires.
     */
    public Integer getExpires() {
        return expires;
    }

    /**
     * Renders the command into a JsonObject for transport across the event bus.
     *
     * @return - A JsonObject containing the command.
     */
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("command", type.getCommand());
        jsonObject.put("key", key);

        if (value != null) {
            jsonObject.put("value", value);
        }

        if (expires != null) {
            jsonObject.put("expires", expires);
        }

        return jsonObject;
    }

    /**
     * Calling this method will execute the handler associate with this command.  If no
     * handler is specified the response will be ignored.
     *
     * @param response - The Memcache response for this command.
     */
    public void setResponse(JsonObject response) {
        if (commandResponseHandler != null) {
            commandResponseHandler.handle(response);
        } else {
            log.trace("setResponse", "missingHandler");
        }
    }

    /**
     * This handler will be executed when the response has been received from Memcache.
     *
     * @param handler - A handler for the JsonObject Memcache response.
     */
    public void commandResponseHandler(Handler<JsonObject> handler) {
        this.commandResponseHandler = handler;
    }

    /**
     * This is the parser for processing the current response for this command.
     *
     * @return - The currently initialized line parser.
     */
    public LineParser getLineParser() {
        return parser;
    }

    /**
     * This initializes the line parser for processing the response for this command.
     *
     * @param lineParserType - The type of line parser to create.
     */
    private void setLineParser(LineParserType lineParserType) {
        switch (lineParserType) {
            case RETRIEVE:
                parser = new RetrieveLineParser();
                break;
            case STORE:
                parser = new StoreLineParser();
                break;
            case MODIFY:
                parser = new ModifyLineParser();
                break;
            case DELETE:
                parser = new DeleteLineParser();
                break;
            case TOUCH:
                parser = new TouchLineParser();
                break;
            default:
                throw new MemcacheException("Unable to initialize line parser.");
        }
    }
}
