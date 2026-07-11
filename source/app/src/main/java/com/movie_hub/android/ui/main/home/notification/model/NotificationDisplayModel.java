package com.movie_hub.android.ui.main.home.notification.model;

import lombok.Data;

@Data
public class NotificationDisplayModel {
    private String title;
    private String subtitle;
    private String preview;
    private String avatarUrl;
    private boolean showAvatar;
    private boolean useAppLogoAvatar;
    private boolean previewMasked;
    private String toxicSpans;
    private Integer toxicStatus;
}
