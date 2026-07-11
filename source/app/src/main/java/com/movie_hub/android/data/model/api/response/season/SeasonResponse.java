package com.movie_hub.android.data.model.api.response.season;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;

import java.util.List;

import lombok.Data;

@Data
public class SeasonResponse {
    private Long id;
    private String title;
    private String description;
    private String label;
    private Integer kind;
    private Integer ordering;
    private Integer status;
    private String releaseDate;
    private String createdDate;
    private String modifiedDate;
    private VideoResponse video;
    private MovieItemResponse trailer;
    private List<MovieItemResponse> episodes;
    private boolean isSelect = false;

    public void setEpisodePlaying(Long episodeId) {
        if (episodes == null || episodeId == null) {
            return;
        }

        for (MovieItemResponse episode : episodes) {
            episode.setPlaying(episode.getId() != null && episode.getId().equals(episodeId));
        }
    }

    public void setAllEpisodePlayingFalse() {
        if (episodes == null) return;
        for (MovieItemResponse e : episodes) {
            e.setPlaying(false);
        }
    }
}

