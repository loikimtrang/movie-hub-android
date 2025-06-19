package com.movie_hub.android.others;

import android.util.Log;

import timber.log.Timber;

public class MyTimberReleaseTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {

    }
}
