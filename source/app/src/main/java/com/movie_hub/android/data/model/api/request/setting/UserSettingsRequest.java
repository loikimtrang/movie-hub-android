package com.movie_hub.android.data.model.api.request.setting;

import lombok.Data;

@Data
public class UserSettingsRequest {
    private Integer audio;
    private Boolean autoNextEpisode;
    private Boolean autoSkipIntro;
    private Integer brightness;

    private Double playbackSpeed;
    private Integer resolution;

    private boolean isSetupBrightness;
    private boolean isSetupPlaybackSpeed;
    private boolean isSetupAudio;
    private boolean isSetupResolution;
}
