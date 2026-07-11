package com.movie_hub.android.data.model.onesignal;

import lombok.Data;

@Data
public class MessageCommentResponse {
    private String id;
    private String movieItemId;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private String content;
    private String parentId;
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
