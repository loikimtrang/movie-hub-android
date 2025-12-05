package com.movie_hub.android.utils;

import android.util.Base64;
import org.json.JSONObject;

public class JwtUtils {
    public static long getExpiryTime(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return -1;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);
            return json.getLong("exp") * 1000; // milliseconds
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean isTokenExpiringSoon(String jwt, long bufferMillis) {
        long expiryTime = getExpiryTime(jwt);
        long now = System.currentTimeMillis();
        return expiryTime > 0 && (expiryTime - now) < bufferMillis;
    }
}

