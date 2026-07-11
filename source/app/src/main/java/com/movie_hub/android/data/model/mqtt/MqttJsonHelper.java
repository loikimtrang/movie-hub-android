package com.movie_hub.android.data.model.mqtt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/** Helpers for MQTT payloads where web may send snowflake IDs as JSON numbers. */
public final class MqttJsonHelper {

    private MqttJsonHelper() {
    }

    public static String readFlexibleString(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        JsonElement el = obj.get(key);
        if (!el.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = el.getAsJsonPrimitive();
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        if (primitive.isNumber()) {
            return primitive.getAsBigDecimal().toPlainString();
        }
        return null;
    }

    public static boolean idsMatch(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        String normalizedLeft = normalizeId(left);
        String normalizedRight = normalizeId(right);
        return !normalizedLeft.isEmpty() && normalizedLeft.equals(normalizedRight);
    }

    public static String normalizeId(String id) {
        if (id == null) {
            return "";
        }
        String trimmed = id.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.indexOf('E') >= 0 || trimmed.indexOf('e') >= 0) {
            try {
                return new java.math.BigDecimal(trimmed).toPlainString();
            } catch (NumberFormatException ignored) {
                return trimmed;
            }
        }
        return trimmed;
    }
}
