package com.movie_hub.android.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.movie_hub.android.data.model.mqtt.RoomStateModel;
import com.movie_hub.android.data.model.mqtt.RoomStateModelDeserializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public final class GsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .serializeNulls()  // giữ luôn key null khi convert
            .create();

    private static final Gson roomStateGson = new GsonBuilder()
            .setLenient()
            .serializeNulls()
            .registerTypeAdapter(RoomStateModel.class, new RoomStateModelDeserializer())
            .create();

    private GsonUtils() {
        // do not instantiate
    }

    /** Convert object bất kỳ sang JSON string */
    public static String toJson(Object object) {
        try {
            return gson.toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Parse JSON string sang object kiểu T */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Parse JSON sang list */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Parse JSON sang map */
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Parse room playback state; accepts int/long/double for numeric fields. */
    public static RoomStateModel fromJsonRoomStateModel(String json) {
        try {
            return roomStateGson.fromJson(json, RoomStateModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Lấy Gson instance nếu cần tùy chỉnh thêm */
    public static Gson getGson() {
        return gson;
    }
}
