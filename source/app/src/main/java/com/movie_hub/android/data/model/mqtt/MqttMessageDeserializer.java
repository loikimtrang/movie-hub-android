package com.movie_hub.android.data.model.mqtt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.movie_hub.android.data.mqtt.Message;

import java.lang.reflect.Type;

/**
 * Keeps {@link Message#getData()} as {@link JsonElement} so large numeric IDs from web
 * are not rounded when Gson first parses the MQTT payload.
 */
public class MqttMessageDeserializer implements JsonDeserializer<Message> {

    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            return null;
        }
        JsonObject obj = json.getAsJsonObject();
        Message message = new Message();
        if (obj.has("cmd") && !obj.get("cmd").isJsonNull()) {
            message.setCmd(obj.get("cmd").getAsString());
        }
        if (obj.has("data") && !obj.get("data").isJsonNull()) {
            message.setData(obj.get("data"));
        }
        return message;
    }
}
