package com.movie_hub.android.data.model.onesignal;

import lombok.Data;

@Data
public class MessageCommentResponse {
    private String id;
    private String movieId;
    private String content;
    private String parentId;
    private Author author;

    @Data
    public static class Author {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String avatarPath;
    }
}
