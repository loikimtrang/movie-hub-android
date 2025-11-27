package com.movie_hub.android.ui.main.movie.detail.comment;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

public class CommentViewModel extends BaseViewModel {
    MovieResponse movieDetails = new MovieResponse();
    public CommentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
