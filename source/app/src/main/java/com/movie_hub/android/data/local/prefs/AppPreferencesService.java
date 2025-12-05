package com.movie_hub.android.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.di.qualifier.PreferenceInfo;
import com.movie_hub.android.utils.LogService;
import com.google.gson.Gson;
import com.google.gson.internal.Primitives;

import javax.inject.Inject;

import timber.log.Timber;
import android.util.Log;
public class AppPreferencesService implements PreferencesService {

    private final SharedPreferences mPrefs;
    private final Gson gson;

    @Inject
    public AppPreferencesService(Context context, @PreferenceInfo String prefFileName, Gson gson) {
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
        this.gson = gson;
    }

    @Override
    public void saveAccessTokenObject(UserLoginResponse userLoginResponse) {
        try {
            String json = gson.toJson(userLoginResponse);
            mPrefs.edit().putString(KEY_ACCESS_TOKEN_OBJECT, json).apply();
        } catch (Exception e) {
            LogService.e(e);
        }
    }

    @Override
    public UserLoginResponse getUserAccessTokenObject() {
        try {
            String json = mPrefs.getString(KEY_ACCESS_TOKEN_OBJECT, null);
            if (json != null) {
                return gson.fromJson(json, UserLoginResponse.class);
            }
        } catch (Exception e) {
            LogService.e(e);
        }
        return null;
    }

    @Override
    public void clearAuthData() {
        removeKey(KEY_BEARER_TOKEN);
        removeKey(KEY_BEARER_REFRESH_TOKEN);
        removeKey(KEY_ACCESS_TOKEN_OBJECT);
        removeKey(KEY_USER_RESPONSE);
        removeKey(KEY_USER_ID);
    }

    @Override
    public String getToken() {
        return mPrefs.getString(KEY_BEARER_TOKEN, Constants.VALUE_BEARER_TOKEN_DEFAULT);
    }

    @Override
    public String getRefreshToken() {
        return mPrefs.getString(KEY_BEARER_REFRESH_TOKEN, Constants.VALUE_BEARER_TOKEN_DEFAULT);
    }

    @Override
    public void setToken(String token) {
        mPrefs.edit().putString(KEY_BEARER_TOKEN, token).apply();
    }

    @Override
    public void setRefreshToken(String refreshToken) {
        mPrefs.edit().putString(KEY_BEARER_REFRESH_TOKEN, refreshToken).apply();
    }

    @Override
    public Long getUserId() {
        return mPrefs.getLong(KEY_USER_ID, Constants.VALUE_USER_ID_DEFAULT);
    }

    @Override
    public void setUserId(Long id) {
        mPrefs.edit().putLong(KEY_USER_ID, id).apply();
    }

    @Override
    public void removeKey(String key) {
        mPrefs.edit().remove(key).apply();
    }

    @Override
    public void removeAllKeys() {
        mPrefs.edit().clear().apply();
    }

    @Override
    public boolean containKey(String key) {
        return mPrefs.contains(key);
    }

    @Override
    public void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void setBoolean(String key, boolean val){
        mPrefs.edit().putBoolean(key, val).apply();
    }
    @Override
    public boolean getBooleanVal(String key) {
        return mPrefs.getBoolean(key, false);
    }

    @Override
    public void setString(String key, String val){
        mPrefs.edit().putString(key, val).apply();
    }
    @Override
    public String getStringVal(String key) {
        return mPrefs.getString(key, null);
    }

    @Override
    public void setInt(String key, int val){
        mPrefs.edit().putInt(key, val).apply();
    }
    @Override
    public int getIntVal(String key) {
        return mPrefs.getInt(key, 0);
    }

    @Override
    public void setLong(String key, long val){
        mPrefs.edit().putLong(key, val).apply();
    }
    @Override
    public long getLongVal(String key) {
        return mPrefs.getLong(key, 0);
    }

    @Override
    public void setFloat(String key, float val){
        mPrefs.edit().putFloat(key, val).apply();
    }
    @Override
    public float getFloatVal(String key) {
        return mPrefs.getFloat(key, 0);
    }

    @Override
    public <T> T getObjectVal(String key, Class<T> mModelClass) {
        Object object = null;
        try {
            object = gson.fromJson(mPrefs.getString(key, ""), mModelClass);
        } catch (Exception ex) {
            LogService.e(ex);
        }
        return Primitives.wrap(mModelClass).cast(object);
    }
    public static final String KEY_LANGUAGE_CODE = "language_code";

    public void setAppLanguage(String langCode) {
        mPrefs.edit().putString(KEY_LANGUAGE_CODE, langCode).apply();
    }

    public String getAppLanguage() {
        return mPrefs.getString(KEY_LANGUAGE_CODE, "vi");
    }

}
