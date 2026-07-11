package com.movie_hub.android.data.model.api.response.room;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarPath;
    private Integer kind;
    private Integer gender;
    private Boolean isVip;
}
