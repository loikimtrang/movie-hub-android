package com.movie_hub.android.data.model.api.response.report;

import lombok.Data;

@Data
public class CreateReportRequest {
    String content;
    Long objectId;
    Integer type;
}
