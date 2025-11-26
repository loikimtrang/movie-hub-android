package com.movie_hub.android.data.model.api.response.favourite;

import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Data;

@Data
public class FavouriteResponse {
    private Long id;
    private String createdDate;
    private String modifiedDate;
    private int status;
    private int type;
    private MovieResponse movie;
    private PersonResponse person;
    private UserResponse user;
}