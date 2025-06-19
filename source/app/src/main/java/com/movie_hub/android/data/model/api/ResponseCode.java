package com.movie_hub.android.data.model.api;

public class ResponseCode {

    public static final int RESPONSE_ERROR_SERVER_ERROR = 500;
    public static final int RESPONSE_SUCCESS = 200;

    public static final int RESPONSE_ERROR_CODE_UNDEFINED = 0;
    public static final int RESPONSE_ERROR_CODE_TIMEOUT = 100;
    public static final int RESPONSE_ERROR_INVALID_PATH = 101;
    public static final int RESPONSE_SESSION_TIMEOUT = 401;
    public static final int RESPONSE_INVALID_CMD = 103;

    private ResponseCode(){
        //do not initial me
    }
}
