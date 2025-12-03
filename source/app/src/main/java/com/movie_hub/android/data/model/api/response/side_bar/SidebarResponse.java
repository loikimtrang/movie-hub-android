package com.movie_hub.android.data.model.api.response.side_bar;

import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;

import lombok.Data;

@Data
public class SidebarResponse {
    private boolean active;
    private String createdDate;
    private String description;
    private Long id;
    private String mainColor;
    private String mobileThumbnailUrl;
    private String modifiedDate;
    private MovieResponse movie;
    private int ordering;
    private int status;
    private String webThumbnailUrl;

    public String getMobileThumbnailUrl() {
        return Constants.MEDIA_URL + mobileThumbnailUrl;
    }
}

