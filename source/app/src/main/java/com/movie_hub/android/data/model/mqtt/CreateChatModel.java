package com.movie_hub.android.data.model.mqtt;

import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Watch-room chat payload for {@link com.movie_hub.android.data.mqtt.Command#CMD_CREATE_CHAT}.
 */
@Data
@NoArgsConstructor
public class CreateChatModel {
    private String accountId;
    private String content;
    private UserResponse author;
    private String createDate;
    /** Local-only: true when the message arrived while the chat panel was closed. */
    private boolean unread;

    public CreateChatModel(String accountId, String content, UserResponse author, String createDate) {
        this.accountId = accountId;
        this.content = content;
        this.author = author;
        this.createDate = createDate;
        this.unread = false;
    }
}
