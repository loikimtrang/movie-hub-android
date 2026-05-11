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

        // 1. Lấy thông tin các header điều hướng
        String ignoreAuth = originalRequest.header("IgnoreAuth");
        String useBasicAuth = originalRequest.header("UseBasicAuth");
        String useGuestToken = originalRequest.header("UseGuestToken");

        // Xóa các header đánh dấu để tránh gửi lên server
        requestBuilder.removeHeader("IgnoreAuth");
        requestBuilder.removeHeader("UseBasicAuth");
        requestBuilder.removeHeader("UseGuestToken");

        // Khai báo biến token ở đây để dùng chung cho toàn bộ hàm intercept (Fix lỗi Resolve Symbol)
        String token = appPreferences.getToken();

        // TRƯỜNG HỢP 1: Bỏ qua định danh hoàn toàn
        if ("1".equals(ignoreAuth)) {
            return chain.proceed(requestBuilder.build());
        }

        // TRƯỜNG HỢP 2: Sử dụng Basic Auth
        if ("1".equals(useBasicAuth)) {
            String credentials = "abc_client:abc123";
            String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
            requestBuilder.header("Authorization", basicAuth);
        }
        else {
            // TRƯỜNG HỢP 3: Xử lý Bearer Token (Login hoặc Guest)
            if (token != null && !token.isEmpty() && !"NULL".equalsIgnoreCase(token)) {
                // Kiểm tra Token sắp hết hạn để Refresh chủ động
                if (JwtUtils.isTokenExpiringSoon(token, 2 * 60 * 1000)) {
                    synchronized (this) {
                        String freshToken = refreshTokenSync();
                        if (freshToken != null) {
                            token = freshToken;
                        }
                    }
                }
                requestBuilder.header("Authorization", "Bearer " + token);
            }
            else if ("1".equals(useGuestToken)) {
                token = Constants.TOKEN_GUEST;
                requestBuilder.header("Authorization", "Bearer " + token);
            }
        }

        // Thực thi Request
        Response response = chain.proceed(requestBuilder.build());

        // 4. Xử lý lỗi 401 (Token hết hạn đột xuất)
        if (response.code() == 401) {
            synchronized (this) {
                String latestToken = appPreferences.getToken();
                String newToken;

                // Kiểm tra xem token hiện tại trong Prefs có khác với token vừa dùng không (đã có luồng khác refresh xong chưa)
                if (latestToken != null && !latestToken.isEmpty() && !latestToken.equals(token)) {
                    newToken = latestToken;
                } else {
                    // Tiến hành Refresh Token thủ công nếu đây là luồng đầu tiên phát hiện 401
                    newToken = refreshTokenSync();
                }

                if (newToken != null) {
                    response.close(); // Quan trọng: Đóng response cũ trước khi thực hiện request mới
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