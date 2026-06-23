package com.movie_hub.android.data.model.mqtt;

import lombok.Data;

/**
 * Server broadcast when watch-room viewer count changes.
 * Topic: {@code room/{roomId}} — cmd {@link com.movie_hub.android.data.mqtt.Command#CMD_UPDATE_PARTICIPANT_COUNT}.
 */
@Data
public class UpdateParticipantCountModel {
    private String roomId;
    private Integer currentViewers;
}
