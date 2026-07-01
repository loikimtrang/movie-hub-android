package com.movie_hub.android.data.model.mqtt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;

/**
 * Accepts {@code currentPositionMovie} and {@code playSpeed} as integer or decimal JSON numbers
 * (mobile sends long, web may send double).
 */
public class RoomStateModelDeserializer implements JsonDeserializer<RoomStateModel> {

    @Override
    public RoomStateModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            return null;
        }
        JsonObject obj = json.getAsJsonObject();
        RoomStateModel model = new RoomStateModel();

        if (obj.has("subCmd") && !obj.get("subCmd").isJsonNull()) {
            model.setSubCmd(obj.get("subCmd").getAsString());
        }
        if (obj.has("isPlay") && !obj.get("isPlay").isJsonNull()) {
            model.setPlay(obj.get("isPlay").getAsBoolean());
        }
        model.setCurrentPositionMovie(readFlexibleNumber(obj, "currentPositionMovie", 0));
        model.setPlaySpeed(readFlexibleNumber(obj, "playSpeed", 1.0));

        return model;
    }

    private static double readFlexibleNumber(JsonObject obj, String key, double defaultValue) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        JsonElement el = obj.get(key);
        if (!el.isJsonPrimitive()) {
            return defaultValue;
        }
        JsonPrimitive primitive = el.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsDouble();
        }
        if (primitive.isString()) {
            try {
                return Double.parseDouble(primitive.getAsString().trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
