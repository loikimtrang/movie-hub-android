package com.movie_hub.android.data.model.onesignal;

import lombok.Data;

@Data
public class MessageRoomNotificationResponse {
    private String id;
    private String code;
    private String name;
    private Integer kind;
    private String movieItemId;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private String startTime;
    private String endTime;
    private AccountNotificationDto host;

    @Data
    public static class AccountNotificationDto {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String avatarPath;
    }
}
