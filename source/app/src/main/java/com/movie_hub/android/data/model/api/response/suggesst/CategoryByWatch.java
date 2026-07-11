package com.movie_hub.android.data.model.api.response.suggesst;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import java.util.List;

import lombok.Data;

@Data
public class CategoryByWatch {
    private CategoryResponse category;
    private List<MovieResponse> movies;

    public CollectionResponse getCollection(String title) {
        if (category != null && category.getId() != null) {
            CollectionResponse collectionResponse = new CollectionResponse();
            collectionResponse.setMovies(movies);
            collectionResponse.setName(title + " " + category.getName());
            collectionResponse.setId(System.currentTimeMillis());
            collectionResponse.setStyleType(Constants.TYPE_COLLECTION_2);
            collectionResponse.setTmpCollection(true);
            return collectionResponse;
        }

        return new CollectionResponse();
    }
}
