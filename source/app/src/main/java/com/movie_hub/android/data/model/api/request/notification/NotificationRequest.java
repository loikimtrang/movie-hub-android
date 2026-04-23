package com.movie_hub.android.data.model.api.request.notification;

import lombok.Data;

@Data
public class NotificationRequest {
    private Integer page;
    private Integer size;
    private Boolean isRead;
}

