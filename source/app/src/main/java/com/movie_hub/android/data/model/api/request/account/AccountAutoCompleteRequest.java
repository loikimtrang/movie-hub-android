package com.movie_hub.android.data.model.api.request.account;

import lombok.Data;

@Data
public class AccountAutoCompleteRequest {
    private String keyword;
    private Long ignoreUserId;
}
