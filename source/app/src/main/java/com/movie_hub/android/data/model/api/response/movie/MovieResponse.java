package com.movie_hub.android.data.model.api.response.movie;

import android.content.Context;
import android.text.Spanned;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.review.ReviewStatisticsResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.ui.main.movie.detail.comment.model.TagComment;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String thumbnailUrl;
    private String imageTitleUrl;
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
    private Long reviewCount;
    private Double averageRating;
    private Long commentCount;

    private boolean isSelect;
    public Double getAverageRating() {
        return averageRating == null ? 0.0 : Math.round(averageRating * 10) / 10.0;
    }

    public Spanned getDescription() {
        return HtmlUtils.convertPtoStrong(description);
    }

    public String getPosterUrl() {
        return Constants.MEDIA_URL + this.posterUrl;
    }
    public String getThumbnailUrl() {
        return Constants.MEDIA_URL + thumbnailUrl;
    }
    public String getImageTitleUrl() {
        return Constants.MEDIA_URL + imageTitleUrl;
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

    public MovieItemResponse getEpisodeById(Long movieItemId) {
        if (seasons == null) return null;

        for (int i = 0; i < seasons.size(); i++) {
            SeasonResponse season = seasons.get(i);
            if (season == null || season.getEpisodes() == null) continue;

            for (MovieItemResponse e : season.getEpisodes()) {
                if (e.getId().equals(movieItemId)) {
                    if (e.getParent() == null) {
                        e.setParent(new SeasonResponse());
                    }

                    e.getParent().setId(season.getId());
                    e.getParent().setLabel(String.valueOf(i + 1));
                    return e;
                }
            }
        }
        return seasons.get(0).getEpisodes().get(0);
    }


    public boolean isLastEpisode(Long episodeId) {
        if (seasons == null || episodeId == null) return false;

        for (int i = seasons.size() - 1; i >= 0; i--) {
            SeasonResponse season = seasons.get(i);
            if (season.getEpisodes() != null && !season.getEpisodes().isEmpty()) {
                List<MovieItemResponse> episodes = season.getEpisodes();
                MovieItemResponse lastEp = episodes.get(episodes.size() - 1);
                return episodeId.equals(lastEp.getId());
            }
        }

        return false;
    }

    public MovieItemResponse getNextEpisode(Long episodeId) {
        if (seasons == null || episodeId == null) return null;

        for (int seasonIndex = 0; seasonIndex < seasons.size(); seasonIndex++) {
            SeasonResponse season = seasons.get(seasonIndex);
            List<MovieItemResponse> episodes = season.getEpisodes();
            if (episodes == null || episodes.isEmpty()) continue;

            for (int epIndex = 0; epIndex < episodes.size(); epIndex++) {
                MovieItemResponse episode = episodes.get(epIndex);
                if (episodeId.equals(episode.getId())) {
                    // Nếu còn tập kế tiếp trong cùng mùa
                    if (epIndex + 1 < episodes.size()) {
                        return episodes.get(epIndex + 1);
                    }

                    // Nếu là tập cuối mùa, chuyển sang mùa kế
                    for (int nextSeasonIndex = seasonIndex + 1; nextSeasonIndex < seasons.size(); nextSeasonIndex++) {
                        SeasonResponse nextSeason = seasons.get(nextSeasonIndex);
                        List<MovieItemResponse> nextEpisodes = nextSeason.getEpisodes();
                        if (nextEpisodes != null && !nextEpisodes.isEmpty()) {
                            return nextEpisodes.get(0); // Tập đầu mùa kế tiếp
                        }
                    }

                    // Không có tập kế tiếp
                    return null;
                }
            }
        }

        return null; // Không tìm thấy tập hiện tại
    }

    public void applyWatchHistory(ListWatchHistoryResponse historyResponse) {
        if (historyResponse == null || historyResponse.getWatchHistories() == null || seasons == null)
            return;

        // Convert watch history thành HashMap để lookup O(1)
        Map<Long, WatchHistoryResponse> map = new HashMap<>();
        for (WatchHistoryResponse h : historyResponse.getWatchHistories()) {
            if (h.getMovieItemId() != null) {
                map.put(h.getMovieItemId(), h);
            }
        }

        // Merge vào episode nhanh nhất
        for (SeasonResponse season : seasons) {
            if (season.getEpisodes() == null) continue;

            for (MovieItemResponse episode : season.getEpisodes()) {
                WatchHistoryResponse history = map.get(episode.getId());
                if (history != null) {
                    episode.setCompleted(history.isCompleted());
                    episode.setLastWatchSeconds(history.getLastWatchSeconds());
                }
            }
        }
    }

    public void setSeasonSelect(Long id) {
        for (SeasonResponse s : seasons) {
            s.setSelect(s.getId().equals(id));
        }
    }

    public int getIndexSeasonSelect() {
        for (int i = 0; i<seasons.size(); i++) {
            if (seasons.get(i).isSelect()) return i;
        }
        return -1;
    }

    public List<TagComment> getListLabelEpisode(Context context) {
        List<TagComment> comments = new ArrayList<>();
        for (int i = 0; i < seasons.size(); i++) {
            for (int j = 0; j < seasons.get(i).getEpisodes().size(); j++) {
                TagComment tagComment = new TagComment();
                tagComment.setMovieId(id);
                tagComment.setMovieItemId(seasons.get(i).getEpisodes().get(j).getId());
                tagComment.setLabel(context.getString(R.string.season_char) + (i + 1) + ":"
                       + context.getString(R.string.episode_char) + (j + 1));

                comments.add(tagComment);
            }
        }

        return comments;
    }
}
