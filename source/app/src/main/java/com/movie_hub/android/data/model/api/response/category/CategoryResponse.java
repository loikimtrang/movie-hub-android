package com.movie_hub.android.data.model.api.response.category;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Integer status;
    private String createdDate;
    private String modifiedDate;
    private boolean isSelect;
}
