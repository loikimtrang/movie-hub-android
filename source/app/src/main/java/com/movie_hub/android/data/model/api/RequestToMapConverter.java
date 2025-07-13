package com.movie_hub.android.data.model.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class RequestToMapConverter {

    public static <T> Map<String, Object> convert(T request) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (Field field : request.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(request);
                if (value != null) {
                    String key = field.getName();

                    if (key.equalsIgnoreCase("sortSorted")) {
                        key = "sort.sorted";
                    } else if (key.equalsIgnoreCase("sortUnsorted")) {
                        key = "sort.unsorted";
                    }

                    map.put(key, value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }
}

