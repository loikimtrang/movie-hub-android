package com.movie_hub.android.data.model.api.response.playlist;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PlayListByMovieResponse {
    private List<Long> ids = new ArrayList<>();
}
