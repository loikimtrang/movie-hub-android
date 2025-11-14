package com.movie_hub.android.ui.main.movie.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

import lombok.Getter;
import lombok.Setter;

public class MovieDetailViewModel extends BaseViewModel {
    public MovieResponse movieDetails;
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);

    public MovieDetailViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }
}
