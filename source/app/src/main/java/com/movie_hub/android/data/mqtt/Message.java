package com.movie_hub.android.data.mqtt;

import lombok.Data;

@Data
//@EqualsAndHashCode
public class Message {
    private String cmd;
    private Object data;
}
