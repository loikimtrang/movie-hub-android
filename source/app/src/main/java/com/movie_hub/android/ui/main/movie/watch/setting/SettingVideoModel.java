package com.movie_hub.android.ui.main.movie.watch.setting;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SettingVideoModel {

    private Quality quality = new Quality();
    private PlaySpeed playSpeed = new PlaySpeed();
    private PlaySpeed playSpeedWhenPress = new PlaySpeed();
    private List<VideoQuality> availableQualities = new ArrayList<>();

    /**
     * Observable current playback speed (single source of truth).
     * Dialogs/Activity can observe this to keep UI + player in sync.
     */
    private transient MutableLiveData<Float> playbackSpeedLive = new MutableLiveData<>();

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
        setPlaybackSpeed(1.0f);
        playSpeedWhenPress.setSpeed(2.0f);
    }

    public MutableLiveData<Float> getPlaybackSpeedLive() {
        if (playbackSpeedLive == null) playbackSpeedLive = new MutableLiveData<>();
        return playbackSpeedLive;
    }

    public void setPlaybackSpeed(float speed) {
        playSpeed.setSpeed(speed);
        getPlaybackSpeedLive().postValue(speed);
    }
}
