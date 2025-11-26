package com.movie_hub.android.data.model.api.response.movie;

import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;

import java.util.List;

import lombok.Data;

@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String thumbnailUrl;
    private String slug;
    private Integer ageRating;
    private Integer status;
    private Integer type;
    private Long viewCount;
    private Boolean isFeatured;
    private String country;
    private String language;
    private String releaseDate;
    private String createdDate;
    private String modifiedDate;
    private List<CategoryResponse> categories;
    private List<SeasonResponse> seasons;
    public String getPosterUrl() {
        return Constants.MEDIA_URL + posterUrl;
    }
    public String getThumbnailUrl() {
        return Constants.MEDIA_URL + thumbnailUrl;
    }
    public void setSeasonAndEpisodeSelectedAndPlaying(Long episodeId) {
        if (episodeId == null || seasons == null) {
            return;
        }

        boolean found = false;

        for (SeasonResponse season : seasons) {
            boolean seasonHasEpisode = false;

            if (season.getEpisodes() != null) {
                for (MovieItemResponse episode : season.getEpisodes()) {
                    if (episode.getId() != null && episode.getId().equals(episodeId)) {
                        seasonHasEpisode = true;
                        found = true;
                        episode.setPlaying(true);
                    } else {
                        episode.setPlaying(false);
                    }
                }
            }

            season.setSelect(seasonHasEpisode);
        }

        if (!found) {
            setAllSeasonSelectFalse();
            for (SeasonResponse s : seasons) {
                s.setAllEpisodePlayingFalse();
            }
        }
    }
    public List<MovieItemResponse> getSelectedSeasonEpisodes() {
        if (seasons == null) return null;

        for (SeasonResponse season : seasons) {
            if (season.isSelect() && season.getEpisodes() != null) {
                return season.getEpisodes();
            }
        }
        return null;
    }

    public List<MovieItemResponse> getSeasonEpisodesById(Long seasonId) {
        if (seasons == null) return null;

        for (SeasonResponse season : seasons) {
            if (season.getId().equals(seasonId)) {
                return season.getEpisodes();
            }
        }
        return null;
    }
    public void setAllSeasonSelectFalse() {
        if (seasons == null) return;
        for (SeasonResponse s : seasons) {
            s.setSelect(false);
        }
    }
}
