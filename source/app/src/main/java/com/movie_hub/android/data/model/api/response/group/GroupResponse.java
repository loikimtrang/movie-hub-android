package com.movie_hub.android.data.model.api.response.group;

import com.movie_hub.android.data.model.api.response.permission.PermissionResponse;

import java.util.List;

import lombok.Data;

@Data
public class GroupResponse {
    private String createdDate;
    private String description;
    private long id;
    private boolean isSystemRole;
    private int kind;
    private String modifiedDate;
    private String name;
    private List<PermissionResponse> permissions;
    private int status;
}
