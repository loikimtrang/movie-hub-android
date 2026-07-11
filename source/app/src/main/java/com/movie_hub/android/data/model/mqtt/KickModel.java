package com.movie_hub.android.data.model.mqtt;

import lombok.Data;

/** Payload for {@link com.movie_hub.android.data.mqtt.Command#CMD_KICK}. */
@Data
public class KickModel {
    private String roomId;
    private String targetUserId;
}
