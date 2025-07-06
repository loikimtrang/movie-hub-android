package com.movie_hub.android.data.model.api.response.permission;

import lombok.Data;

@Data
public class PermissionResponse {
    private String createdDate;
    private long id;
    private String modifiedDate;
    private String permissionCode;
    private int status;
}
