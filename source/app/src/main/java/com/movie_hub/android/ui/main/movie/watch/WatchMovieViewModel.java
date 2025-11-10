package com.movie_hub.android.ui.main.movie.watch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;

public class WatchMovieViewModel extends BaseViewModel {
    private final MutableLiveData<MovieResponse> movieDetails = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);

    SettingVideoModel settingVideoModel = new SettingVideoModel();
    public WatchMovieViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
        settingVideoModel.initSetting();
    }

    public LiveData<MovieResponse> getMovieDetails() {
        return movieDetails;
    }

    public void setMovieDetails(MovieResponse movie) {
        movieDetails.setValue(movie);
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }
}
