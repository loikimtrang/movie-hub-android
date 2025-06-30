package com.movie_hub.android.ui.main;

import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.ui.base.activity.BaseCallback;

public interface MainCallback<T> extends BaseCallback {
    default void doSuccess(T object) {

    }
    default void doErrorForm(ResponseWrapper response) {

    }
}