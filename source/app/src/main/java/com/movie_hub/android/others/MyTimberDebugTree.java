package com.movie_hub.android.others;

import timber.log.Timber;

public class MyTimberDebugTree extends Timber.DebugTree{
    @Override
    protected String createStackElementTag(StackTraceElement element) {
        return String.format("[L:%s] [M:%s] [C:%s] **",
                element.getLineNumber(),
                element.getMethodName(),
                super.createStackElementTag(element));
    }
}
