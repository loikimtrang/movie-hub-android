package com.movie_hub.android.data.remote;

import android.app.Application;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.local.prefs.PreferencesService;
import com.movie_hub.android.utils.LogService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final PreferencesService appPreferences;
    private final Application application;

    public AuthInterceptor(PreferencesService appPreferences, Application application) {
        this.appPreferences = appPreferences;
        this.application = application;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder newRequest = originalRequest.newBuilder();

        if ("1".equals(originalRequest.header("IgnoreAuth"))) {
            newRequest.removeHeader("IgnoreAuth");
            newRequest.addHeader("X-tenant", "1235");
            return chain.proceed(newRequest.build());
        }

        if ("1".equals(originalRequest.header("UseBasicAuth"))) {
            newRequest.removeHeader("UseBasicAuth");

            String username = "abc_client";
            String password = "abc123";
            String credentials = username + ":" + password;

            String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
            newRequest.addHeader("Authorization", basicAuth);
        } else {
            String token = appPreferences.getToken();
            if (token != null && !token.isEmpty() && !"NULL".equalsIgnoreCase(token)) {
                newRequest.addHeader("Authorization", "Bearer " + token);
            }
        }

        newRequest.addHeader("X-tenant", "1235");

        Response response = chain.proceed(newRequest.build());

        if (response.code() == 401 || response.code() == 403) {
            LogService.i("Auth error code: " + response.code());
            appPreferences.removeKey(PreferencesService.KEY_BEARER_TOKEN);

            Intent intent = new Intent(Constants.ACTION_EXPIRED_TOKEN);
            LocalBroadcastManager.getInstance(application.getApplicationContext()).sendBroadcast(intent);
        }

        return response;
    }
}
