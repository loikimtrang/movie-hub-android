package com.movie_hub.android.data.model.mqtt;

import lombok.Data;

@Data
public class RoomStateModel {
    String subCmd;

    boolean isPlay;
    double currentPositionMovie;
    double playSpeed;
}
