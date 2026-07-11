package com.movie_hub.android.data.model.api.response.appversion;

import com.movie_hub.android.constant.Constants;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppVersionResponse {
    private String changeLog;
    private Integer code;
    private String createdDate;
    private String filePath;
    private Boolean forceUpdate;
    private Long id;
    private Boolean isLatest;
    private String modifiedDate;
    private String name;
    private Integer status;

    public String getUrl() {
        return Constants.MEDIA_URL + filePath;
    }
}

