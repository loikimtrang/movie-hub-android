package com.movie_hub.android.data.remote;

import android.app.Application;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.movie_hub.android.BuildConfig;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.local.prefs.PreferencesService;
import com.movie_hub.android.data.model.api.request.user.RefreshTokenRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.utils.JwtUtils;
import com.movie_hub.android.utils.LogService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final PreferencesService appPreferences;
    private final Application application;
    private final retrofit2.Retrofit refreshRetrofit;
    private final MasterApiService refreshApiService;

    public AuthInterceptor(PreferencesService appPreferences, Application application) {
        this.appPreferences = appPreferences;
        this.application = application;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    String credentials = "abc_client:abc123";
                    String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
                    builder.header("Authorization", basicAuth);
                    builder.header("X-tenant", "moviehub");
                    return chain.proceed(builder.build());
                })
                .build();

        this.refreshRetrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(BuildConfig.MASTER_URL)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .client(client)
                .build();

        this.refreshApiService = refreshRetrofit.create(MasterApiService.class);
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // 1. Xử lý Header X-tenant cho tất cả request
        requestBuilder.header("X-tenant", "moviehub");

        // 2. Kiểm tra các Header đặc biệt (IgnoreAuth/UseBasicAuth)
        if ("1".equals(originalRequest.header("IgnoreAuth"))) {
            requestBuilder.removeHeader("IgnoreAuth");
            return chain.proceed(requestBuilder.build());
        }

        if ("1".equals(originalRequest.header("UseBasicAuth"))) {
            requestBuilder.removeHeader("UseBasicAuth");
            String credentials = "abc_client:abc123";
            String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
            requestBuilder.header("Authorization", basicAuth);
        } else {
            String token = appPreferences.getToken();
            if (token != null && !token.isEmpty() && !"NULL".equalsIgnoreCase(token)) {
                if (JwtUtils.isTokenExpiringSoon(token, 2 * 60 * 1000)) {
                    synchronized (this) {
                        String freshToken = refreshTokenSync();
                        if (freshToken != null) token = freshToken;
                    }
                }
                requestBuilder.header("Authorization", "Bearer " + token);
            }
        }

        Response response = chain.proceed(requestBuilder.build());

        if (response.code() == 401) {
            synchronized (this) {
                String latestToken = appPreferences.getToken();
                String newToken;

                if (latestToken != null && !latestToken.equals(appPreferences.getToken())) {
                    newToken = latestToken;
                } else {
                    newToken = refreshTokenSync();
                }

                if (newToken != null) {
                    response.close();
                    Request retryRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + newToken)
                            .header("X-tenant", "moviehub")
                            .build();

                    LogService.i("Retrying request with new token...");
                    return chain.proceed(retryRequest);
                } else {
                    LogService.e("Refresh Token failed. User session expired.");
                    handleLogout();
                }
            }
        }

        return response;
    }

    private void handleLogout() {
        appPreferences.clearAuthData();
        Intent intent = new Intent(Constants.ACTION_EXPIRED_TOKEN);
        LocalBroadcastManager.getInstance(application.getApplicationContext()).sendBroadcast(intent);
    }

    private synchronized String refreshTokenSync() {
        String refreshToken = appPreferences.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) return null;

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setGrant_type("refresh_token");
        request.setRefresh_token(refreshToken);

        try {
            retrofit2.Response<UserLoginResponse> response = refreshApiService.refreshTokenSync(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String newAccessToken = response.body().getAccess_token();
                String newRefreshToken = response.body().getRefresh_token();

                appPreferences.setToken(newAccessToken);
                appPreferences.setRefreshToken(newRefreshToken);

                LogService.i("Refresh token successful!");
                return newAccessToken;
            }
        } catch (Exception e) {
            LogService.e("Error during refresh: " + e.getMessage());
        }
        return null;
    }
}