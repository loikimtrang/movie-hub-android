package com.movie_hub.android.ui.main.movie.watch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;

public class WatchMovieViewModel extends BaseViewModel {
    public MovieResponse movieDetails;
    public String nowUriPlay;
    public MovieItemResponse nowEpisodePlay;
    public VideoResponse nowVideoPlay; // Single movie

    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);

    SettingVideoModel settingVideoModel = new SettingVideoModel();
    public WatchMovieViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
        settingVideoModel.initSetting();
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }

    public void updateSettingWhenChangeEpisode() {
        settingVideoModel.getQuality().setAuto(true);
        settingVideoModel.getQuality().setResolution(new VideoQuality());
        settingVideoModel.getAvailableQualities().clear();
    }
}
