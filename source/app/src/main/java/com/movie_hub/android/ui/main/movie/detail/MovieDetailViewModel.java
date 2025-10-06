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
    private final MutableLiveData<MovieResponse> movieDetails = new MutableLiveData<>();

    public void setMovieDetails(MovieResponse movie) {
        movieDetails.setValue(movie);
    }

    public LiveData<MovieResponse> getMovieDetails() {
        return movieDetails;
    }

    public MovieDetailViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
