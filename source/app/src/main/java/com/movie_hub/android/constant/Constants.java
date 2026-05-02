package com.movie_hub.android.constant;

import android.os.CpuUsageInfo;

import com.movie_hub.android.BuildConfig;

public class Constants {
    public static final String DB_NAME = "room";
    public static final String PREF_NAME = "mvvm.prefs";
    public static final String VALUE_BEARER_TOKEN_DEFAULT="NULL";
    public static final Long VALUE_USER_ID_DEFAULT = -1L;
    public static final String HOME = "HOME";
    public static final String SEARCH = "SEARCH";
    public static final String LIVE = "LIVE";

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
    public static final String ERROR = "ERROR";
    public static final int RC_SIGN_IN = 1001;
    public static final int SRC_TYPE_VIDEO_EXTERNAL = 2; // có link http
    public static final int SRC_TYPE_VIDEO_INTERNAL = 1; // sài link nhà


    public static final String CLIENT_ID = BuildConfig.CLIENT_ID;
    public static final String ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID;
    public static int PLATFORM_ANDROID = 2;
    public static int TYPE_MOVIE_SINGLE = 1;
    public static int TYPE_MOVIE_SERIES = 2;
    public static int TYPE_MOVIE_TRAILER = 3;
    public static int TYPE_GENRE = 3;

    public static int MOVIE_ITEM_KIND_SEASON = 1;
    public static int MOVIE_ITEM_KIND_EPISODE = 2;
    public static int MOVIE_ITEM_KIND_TRAILER = 3;
    public static final int AGE_RATING_P = 1;
    public static final int AGE_RATING_K = 2;
    public static final int AGE_RATING_T13 = 3;
    public static final int AGE_RATING_T16 = 4;
    public static final int AGE_RATING_T18 = 5;

    public static final int FAVOURITE_TYPE_MOVIE = 1;
    public static final int FAVOURITE_TYPE_PERSON = 2;

    public static float SHIMMER_START_ALPHA = 0.4f;
    public static float SHIMMER_END_ALPHA = 0.08f;
    public static final Integer REACTION_TYPE_LIKE = 1;
    public static final Integer REACTION_TYPE_DISLIKE = 2;
    public static final int MaxPlaylist = 5;
    public static final int MaxLengthNamePlaylist = 25;

    public static final int TYPE_COLLECTION_1 = 1;
    public static final int TYPE_COLLECTION_2 = 2;
    public static final int TYPE_COLLECTION_3 = 3;
    public static final int TYPE_COLLECTION_4 = 4;

    public static final int TYPE_RATING_1 = 1;
    public static final int TYPE_RATING_2 = 2;
    public static final int TYPE_RATING_3 = 3;
    public static final int TYPE_RATING_4 = 4;
    public static final int TYPE_RATING_5 = 5;
    public static String CODE_ACCOUNT_LOCK = "ERROR-ACCOUNT-ERROR-0007";

    public static final Integer ROOM_KIND_PRIVATE = 0;
    public static final Integer ROOM_KIND_PUBLIC = 1;

    public static final Integer ROOM_STATE_PENDING = 0;
    public static final Integer ROOM_STATE_RUNNING = 1;
    public static final Integer ROOM_STATE_ENDING = 2;

    // Aliases used by UI filters (Room state)
    public static final Integer STATE_LOCKED = ROOM_STATE_PENDING;
    public static final Integer STATE_OPEN = ROOM_STATE_RUNNING;
    public static final Integer STATE_END = ROOM_STATE_ENDING;

    public static final Integer PARTICIPANT_ROLE_GUEST = 0;
    public static final Integer PARTICIPANT_ROLE_HOST = 1;

    public static final Integer PARTICIPANT_STATE_PENDING = 0;
    public static final Integer PARTICIPANT_STATE_JOIN = 1;
    public static final Integer PARTICIPANT_STATE_LEFT = 2;
    public static final String MEDIA_URL = BuildConfig.MEDIA_URL;
    public static final String MEDIA_URL_VTT = BuildConfig.MEDIA_URL_VTT;
    public static final String MEDIA_URL_VIDEO = BuildConfig.MEDIA_URL_VIDEO;

    public static final String MQTT_BROKER = BuildConfig.MQTT_BROKER;
    public static final String MQTT_USERNAME = BuildConfig.MQTT_USERNAME;
    public static final String MQTT_PASSWORD = BuildConfig.MQTT_PASSWORD;
    public static final String TOPIC = "room/";

    public static final Integer NOTIFICATION_TYPE_MOVIE = 2;
    public static final Integer NOTIFICATION_TYPE_SOCIAL = 3;

    private Constants(){

    }
}
