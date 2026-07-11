package com.movie_hub.android.data.model.api.response.style;

import lombok.Data;

@Data
public class StyleResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer status;
    private Integer type;
    private Boolean isDefault;
    private String createdDate;
    private String modifiedDate;
}

