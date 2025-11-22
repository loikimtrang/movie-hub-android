package com.movie_hub.android.constant;

import android.os.CpuUsageInfo;

public class Constants {
    public static final String DB_NAME = "room";
    public static final String PREF_NAME = "mvvm.prefs";
    public static final String VALUE_BEARER_TOKEN_DEFAULT="NULL";
    public static final Long VALUE_USER_ID_DEFAULT = -1L;
    public static final String HOME = "HOME";
    public static final String SEARCH = "SEARCH";
    public static final String SCHEDULE = "SCHEDULE";
    public static final String ACCOUNT = "ACCOUNT";
    public static final String ACCOUNT_UN_LOGIN = "ACCOUNT_UN_LOGIN";

    public static final String ACTIVITY_MANAGE_ACCOUNT = "ACTIVITY_MANAGE_ACCOUNT";



    //Local Action manager
    public static final String ACTION_EXPIRED_TOKEN ="ACTION_EXPIRED_TOKEN";
    public static final String INSTAGRAM_LOGIN_URL = "https://www.instagram.com/accounts/login/";
    public static final String INSTAGRAM_URL = "https://www.instagram.com/";
    public static final int REQUEST_LANGUAGE = 1001;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    public static final int GENDER_UNSPECIFIED = 3;

    public static final String MEDIA_URL = "https://media.moviehub.biz/v1/file/download";
    public static final String MEDIA_URL_VTT = "https://media.moviehub.biz/v1/file/public-download"; // vtt,
    public static final String MEDIA_URL_VIDEO = "https://media.moviehub.biz/v1/file/download-video-resource";

    public static final String ERROR = "ERROR";
    public static final int RC_SIGN_IN = 1001;

    public static final String CLIENT_ID = "144532728035-rbu66k0hirdckcp1ato2kff2m83uta4g.apps.googleusercontent.com";
    public static int PLATFORM_ANDROID = 2;
    public static int TYPE_MOVIE_SINGLE = 1;
    public static int TYPE_MOVIE_SERIES = 2;
    public static int TYPE_MOVIE_TRAILER = 3;
    public static int MOVIE_ITEM_KIND_SEASON = 1;
    public static int MOVIE_ITEM_KIND_EPISODE = 2;
    public static int MOVIE_ITEM_KIND_TRAILER = 3;
    public static final int AGE_RATING_G = 1;
    public static final int AGE_RATING_PG = 2;
    public static final int AGE_RATING_PG13 = 3;
    public static final int AGE_RATING_R = 4;
    public static final int AGE_RATING_NC17 = 5;
    public static final int AGE_RATING_18 = 6;
    private Constants(){

    }
}
