package com.movie_hub.android.data.model.onesignal;

import lombok.Data;

@Data
public class MessageOneSignal {
    String cmd;
    String data;
    String title;
    String content;
}
