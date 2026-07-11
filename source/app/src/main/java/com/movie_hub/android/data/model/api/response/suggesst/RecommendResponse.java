package com.movie_hub.android.data.model.api.response.suggesst;

import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RecommendResponse {
    List<MovieResponse> movies = new ArrayList<>();

    public CollectionResponse getCollection(String title) {
        CollectionResponse collectionResponse = new CollectionResponse();

        if (!movies.isEmpty()) {
            collectionResponse.setMovies(movies);
            collectionResponse.setName(title);
            collectionResponse.setId(System.currentTimeMillis());
            collectionResponse.setStyleType(Constants.TYPE_COLLECTION_2);
            collectionResponse.setTmpCollection(true);
        }
        return collectionResponse;
    }
}
