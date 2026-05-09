package com.movie_hub.android.data.model.api.response.suggesst;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import java.util.List;

import lombok.Data;

@Data
public class CategoryByWatch {
    private CategoryResponse category;
    private List<MovieResponse> movies;
}
