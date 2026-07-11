package com.movie_hub.android.data.model.api.response.appversion;

import lombok.Data;

@Data
public class CheckAppVersionResponse {
    private Boolean forceUpdate;
    private Boolean updateRequired;
    private AppVersionResponse latestVersion;
}

