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
    private UserResponse user;
    private String createdDate;
    private transient Boolean isRead;

    public CreateChatModel(String accountId, String content, UserResponse user, String createdDate, Boolean isRead) {
        this.accountId = accountId;
        this.content = content;
        this.user = user;
        this.createdDate = createdDate;
        this.isRead = isRead;
    }
}
