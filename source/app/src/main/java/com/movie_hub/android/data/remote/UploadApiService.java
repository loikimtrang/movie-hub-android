package com.movie_hub.android.data.remote;

import android.app.Application;

import com.google.gson.GsonBuilder;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.local.prefs.AppPreferencesService;
import com.movie_hub.android.data.local.prefs.PreferencesService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class UploadApiService {

    private static final String BASE_URL = "https://media.moviehub.biz";
    private static UploadApiService instance;
    private final ApiService api;
    PreferencesService preferencesService;
    private UploadApiService(Application application) {
        preferencesService = new AppPreferencesService(
                application.getApplicationContext(),
                Constants.PREF_NAME,
                new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        );

        AuthInterceptor authInterceptor = new AuthInterceptor(preferencesService, application);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Timber.tag("OkHttp").i(message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);
    }

    public static void init(Application application) {
        if (instance == null) {
            instance = new UploadApiService(application);
        }
    }

    public static UploadApiService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UploadApiService chưa được init, gọi UploadApiService.init(application) trước");
        }
        return instance;
    }

    public ApiService getApi() {
        return api;
    }
}
