package com.movie_hub.android.data.model.api;

import lombok.Getter;

@Getter
public class ResponseWrapper<T> {
    private boolean result;
    private T data;
    private String message;
    private String code;
    private Integer httpCode;

    private String firebaseUrl;
    private String urlBase;
}
