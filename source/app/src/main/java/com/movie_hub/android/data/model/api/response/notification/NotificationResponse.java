package com.movie_hub.android.data.model.api.response.notification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String body;
    private String cmd;
    private String data;
    private Integer type;
    private Integer status;
    private boolean isRead;
    private String createdDate;
    private String modifiedDate;
}