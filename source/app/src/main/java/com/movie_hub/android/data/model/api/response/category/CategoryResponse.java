package com.movie_hub.android.data.model.api.response.category;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Integer status;
    private String createdDate;
    private String modifiedDate;
}

