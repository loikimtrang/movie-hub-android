package com.movie_hub.android.ui.main.movie.detail.comment.dif;

import androidx.recyclerview.widget.DiffUtil;

import com.movie_hub.android.data.model.api.response.comment.CommentResponse;

import java.util.List;

public class CommentDiffCallback extends DiffUtil.Callback {

    private final List<CommentResponse> oldList;
    private final List<CommentResponse> newList;

    public CommentDiffCallback(List<CommentResponse> oldList, List<CommentResponse> newList) {
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
        return oldList.get(oldItemPosition).getId() ==
                newList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}

