package com.movie_hub.android.data.model.mqtt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

/** Accepts {@code roomId} and {@code targetUserId} as string or JSON number (web). */
public class KickModelDeserializer implements JsonDeserializer<KickModel> {

    @Override
    public KickModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            return null;
        }
        JsonObject obj = json.getAsJsonObject();
        KickModel model = new KickModel();
        model.setRoomId(MqttJsonHelper.readFlexibleString(obj, "roomId"));
        model.setTargetUserId(MqttJsonHelper.readFlexibleString(obj, "targetUserId"));
        return model;
    }
}
