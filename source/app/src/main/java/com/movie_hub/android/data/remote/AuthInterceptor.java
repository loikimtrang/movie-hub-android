package com.movie_hub.android.data.remote;

import android.app.Application;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

                    // Thêm Basic Auth
                    String credentials = "abc_client:abc123";
                    String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
                    builder.header("Authorization", basicAuth);
                    builder.header("UseBasicAuth", "1");
                    builder.header("X-tenant", "moviehub");

                    return chain.proceed(builder.build());
                })
                .build();

        this.refreshRetrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("https://master.moviehub.biz/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .client(client)
                .build();

        this.refreshApiService = refreshRetrofit.create(MasterApiService.class);
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder newRequest = originalRequest.newBuilder();

        if ("1".equals(originalRequest.header("IgnoreAuth"))) {
            newRequest.removeHeader("IgnoreAuth");
            newRequest.addHeader("X-tenant", "moviehub");
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
                if (JwtUtils.isTokenExpiringSoon(token, 2 * 60 * 1000)) {
                    String newToken = refreshTokenSync();
                    if (newToken != null) token = newToken;
                }
                long exp = JwtUtils.getExpiryTime(token);
                LogService.i("Token sắp hết hạn lúc: " + new java.util.Date(exp));
                newRequest.addHeader("Authorization", "Bearer " + token);
            }
        }

        newRequest.addHeader("X-tenant", "moviehub");

        Response response = chain.proceed(newRequest.build());

        if (response.code() == 401 || response.code() == 403) {
            LogService.i("Token expired or unauthorized. Logging out…");
            appPreferences.clearAuthData(); // Xoá cả access lẫn refresh token

            Intent intent = new Intent(Constants.ACTION_EXPIRED_TOKEN);
            LocalBroadcastManager.getInstance(application.getApplicationContext()).sendBroadcast(intent);
        }

        return response;
    }

    private synchronized String refreshTokenSync() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setGrant_type("refresh_token");
        request.setRefresh_token(appPreferences.getRefreshToken());

        try {
            retrofit2.Call<UserLoginResponse> call = refreshApiService.refreshTokenSync(request);
            retrofit2.Response<UserLoginResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                String newAccessToken = response.body().getAccess_token();
                String newRefreshToken = response.body().getRefresh_token();

                appPreferences.setToken(newAccessToken);
                appPreferences.setRefreshToken(newRefreshToken);

                return newAccessToken;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
