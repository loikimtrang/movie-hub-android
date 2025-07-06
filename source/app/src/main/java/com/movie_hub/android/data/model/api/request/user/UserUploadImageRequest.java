package com.movie_hub.android.data.model.api.request.user;

import java.io.File;

import lombok.Data;

@Data
public class UserUploadImageRequest {
    File file;
    String type = "AVATAR";
}
