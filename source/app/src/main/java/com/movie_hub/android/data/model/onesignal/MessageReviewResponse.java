package com.movie_hub.android.data.model.onesignal;

import lombok.Data;

@Data
public class MessageReviewResponse {
    private String id;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private Integer rate;
    private String content;
    private Integer reactionType;
    private Author author;
    private String toxicSpans;

    @Data
    public static class Author {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String avatarPath;
    }
}
