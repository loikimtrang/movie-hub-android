package com.movie_hub.android.ui.main.movie.watch.setting;

public class VideoQuality {
    public String label;

    public int height;
    public int bitrate;
    public String groupIndex;
    public int trackIndex;
    public boolean isCheck = false;
    public VideoQuality() {
    }
    public VideoQuality(int height, int bitrate, String groupIndex, int trackIndex) {
        this.label = getQualityLabel(height);
        this.height = height;
        this.bitrate = bitrate;
        this.groupIndex = groupIndex;
        this.trackIndex = trackIndex;
    }
    public String getQualityLabel(int height) {
        if (height <= 144) return "144P";
        if (height <= 240) return "240P";
        if (height <= 360) return "360P";
        if (height <= 480) return "480P";
        if (height <= 720) return "720P";
        if (height <= 1080) return "1080P";
        if (height <= 1440) return "1440P 2K";
        if (height <= 2160) return "2160P 4K";
        if (height <= 4320) return "4320P 8K";
        return height + "P (Custom)";
    }
}
