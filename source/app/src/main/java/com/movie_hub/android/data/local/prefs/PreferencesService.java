package com.movie_hub.android.data.local.prefs;

import android.content.SharedPreferences;

import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Lombok;

public interface PreferencesService {
    public static final String KEY_BEARER_TOKEN="KEY_BEARER_TOKEN";
    public static final String KEY_BEARER_REFRESH_TOKEN="KEY_BEARER_REFRESH_TOKEN";
    String KEY_ACCESS_TOKEN_OBJECT = "KEY_ACCESS_TOKEN_OBJECT";
    String KEY_USER_RESPONSE = "KEY_USER_RESPONSE";
    String KEY_USER_ID = "KEY_USER_ID";
    String KEY_USER_SETTING= "KEY_USER_SETTING";

    void saveAccessTokenObject(UserLoginResponse userLoginResponse);
    UserLoginResponse getUserAccessTokenObject();
    void clearAuthData();
    String getToken();
    String getRefreshToken();
    void setToken(String token);
    void setRefreshToken(String refreshToken);

    Long getUserId();
    void setUserId(Long id);

    void removeKey(String key);
    void removeAllKeys();
    boolean containKey(String key);
    void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);
    void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);

    void setBoolean(String key, boolean val);
    boolean getBooleanVal(String key);

    void setString(String key, String val);
    String getStringVal(String key);

    void setInt(String key, int val);
    int getIntVal(String key);

    void setLong(String key, long val);
    long getLongVal(String key);

    void setFloat(String key, float val);
    float getFloatVal(String key);

    <T> T getObjectVal(String key, Class<T> mModelClass);
    public void setAppLanguage(String langCode);
    public String getAppLanguage();
}
