package com.movie_hub.android.data.model.mqtt;

import com.movie_hub.android.data.model.api.response.room.ParticipantDto;

import java.util.List;

import lombok.Data;

/**
 * Server broadcast when watch-room viewer count changes.
 * Topic: {@code room/{roomId}} — cmd {@link com.movie_hub.android.data.mqtt.Command#CMD_UPDATE_PARTICIPANT_COUNT}.
 */
@Data
public class UpdateParticipantCountModel {
    private String roomId;
    private Integer currentViewers;
    private List<ParticipantDto> participants;
}
