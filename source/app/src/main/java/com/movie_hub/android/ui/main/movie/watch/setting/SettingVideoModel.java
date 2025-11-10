package com.movie_hub.android.ui.main.movie.watch.setting;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SettingVideoModel {

    private Quality quality = new Quality();
    private PlaySpeed playSpeed = new PlaySpeed();
    private PlaySpeed playSpeedWhenPress = new PlaySpeed();
    private List<VideoQuality> availableQualities = new ArrayList<>();
    @Data
    public static class Quality {
        private boolean isAuto;
        private VideoQuality resolution;
    }

    @Data
    public static class PlaySpeed {
        private Float speed;
    }

    public void initSetting() {
        quality.setAuto(true);
        playSpeed.setSpeed(1.0f);
        playSpeedWhenPress.setSpeed(2.0f);
    }
}
