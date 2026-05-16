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
    private SubtitleStyle subtitleStyle = new SubtitleStyle();

    /**
     * Observable current playback speed (single source of truth).
     * Dialogs/Activity can observe this to keep UI + player in sync.
     */
    private transient MutableLiveData<Float> playbackSpeedLive = new MutableLiveData<>();
    private transient MutableLiveData<SubtitleStyle> subtitleStyleLive = new MutableLiveData<>();

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
        getSubtitleStyleLive().postValue(subtitleStyle);
    }

    public MutableLiveData<Float> getPlaybackSpeedLive() {
        if (playbackSpeedLive == null) playbackSpeedLive = new MutableLiveData<>();
        return playbackSpeedLive;
    }

    public void setPlaybackSpeed(float speed) {
        playSpeed.setSpeed(speed);
        getPlaybackSpeedLive().postValue(speed);
    }

    public MutableLiveData<SubtitleStyle> getSubtitleStyleLive() {
        if (subtitleStyleLive == null) subtitleStyleLive = new MutableLiveData<>();
        return subtitleStyleLive;
    }

    public void updateSubtitleStyle(SubtitleStyle style) {
        if (style == null) return;
        this.subtitleStyle = style;
        getSubtitleStyleLive().postValue(style);
    }
}
