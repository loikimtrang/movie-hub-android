package com.movie_hub.android.data.model.api.response.moviePerson;

import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;

import lombok.Data;

@Data
public class MoviePersonResponse {
    private String characterName;
    private String createdDate;
    private Long id;
    private Integer kind;
    private String modifiedDate;
    private MovieResponse movie;
    private Integer ordering;
    private PersonResponse person;
    private Integer status;
}
