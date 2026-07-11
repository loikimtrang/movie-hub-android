package com.movie_hub.android.data.model.mqtt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

/** Accepts participant {@code id} as string or JSON number (web). */
public class ParticipantJoinModelDeserializer implements JsonDeserializer<ParticipantJoinModel> {

    @Override
    public ParticipantJoinModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            return null;
        }
        JsonObject obj = json.getAsJsonObject();
        ParticipantJoinModel model = new ParticipantJoinModel();
        model.setId(MqttJsonHelper.readFlexibleString(obj, "id"));
        return model;
    }
}
