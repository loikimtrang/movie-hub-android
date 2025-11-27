package com.movie_hub.android.ui.main.movie.detail.diff;

import androidx.recyclerview.widget.DiffUtil;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;

import java.util.List;
import java.util.Objects;

public class EpisodeDiffCallback extends DiffUtil.Callback {

    private final List<MovieItemResponse> oldList;
    private final List<MovieItemResponse> newList;

    public EpisodeDiffCallback(List<MovieItemResponse> oldList, List<MovieItemResponse> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        boolean sameId = Objects.equals(
                oldList.get(oldItemPosition).getId(),
                newList.get(newItemPosition).getId()
        );

        android.util.Log.d("DIFF_UTIL",
                "Checking items same? old=" + oldList.get(oldItemPosition).getId()
                        + ", new=" + newList.get(newItemPosition).getId()
                        + " → " + sameId
        );

        return sameId;
    }


    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        MovieItemResponse oldItem = oldList.get(oldItemPosition);
        MovieItemResponse newItem = newList.get(newItemPosition);

        boolean sameCompleted = Objects.equals(oldItem.isCompleted(), newItem.isCompleted());
        boolean sameProgress = Objects.equals(oldItem.getLastWatchSeconds(), newItem.getLastWatchSeconds());

        // Debug log
        android.util.Log.d("DIFF_UTIL",
                "Episode ID: " + oldItem.getId() + " | Completed: old=" + oldItem.isCompleted() + ", new=" + newItem.isCompleted()
                        + " | Progress: old=" + oldItem.getLastWatchSeconds() + ", new=" + newItem.getLastWatchSeconds()
                        + " | sameCompleted=" + sameCompleted + ", sameProgress=" + sameProgress);

        return sameCompleted && sameProgress;
    }


}
