package com.movie_hub.android.ui.main.movie.watch;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

public class WatchMovieViewModel extends BaseViewModel {
    public WatchMovieViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
