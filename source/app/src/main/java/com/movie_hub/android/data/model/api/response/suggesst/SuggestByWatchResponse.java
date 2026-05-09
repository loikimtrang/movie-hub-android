package com.movie_hub.android.data.model.api.response.suggesst;
import android.content.Context;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import java.util.List;

import lombok.Data;

@Data
public class SuggestByWatchResponse {
    List<MovieResponse> suggestedMovies;
    MovieResponse watchedMovie;

    public CollectionResponse getCollection(String title) {
        if (watchedMovie != null && watchedMovie.getId() != null) {
            CollectionResponse collectionResponse = new CollectionResponse();
            collectionResponse.setMovies(suggestedMovies);
            collectionResponse.setName(title + " " + watchedMovie.getTitle());
            collectionResponse.setId(System.currentTimeMillis());
            collectionResponse.setStyleType(Constants.TYPE_COLLECTION_2);
            collectionResponse.setTmpCollection(true);
            return collectionResponse;
        }

        return new CollectionResponse();
    }
}
