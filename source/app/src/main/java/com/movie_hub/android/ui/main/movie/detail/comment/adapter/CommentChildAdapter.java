package com.movie_hub.android.ui.main.movie.detail.comment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.databinding.ItemCommentChildBinding;
import com.movie_hub.android.databinding.ItemCommentChildBinding;

import java.util.ArrayList;
import java.util.List;

public class CommentChildAdapter extends RecyclerView.Adapter<CommentChildAdapter.CommentChildViewHolder> {

    private final List<CommentResponse> items = new ArrayList<>();
    private OnCommentChildClickListener listener;
    private Context context;
    public interface OnCommentChildClickListener {
        void onLikeClick(CommentResponse commentResponse);
        void onDislikeClick(CommentResponse commentResponse);

    }

    public CommentChildAdapter(OnCommentChildClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCommentChildBinding binding = ItemCommentChildBinding.inflate(inflater, parent, false);
        return new CommentChildViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull CommentChildViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_bottom);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, items.size());
        }
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull CommentChildViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    @SuppressLint("NewApi")
    public void setData(List<CommentResponse> newData) {
        items.clear();

        if (newData != null) {
            items.addAll(newData);
        }

        notifyDataSetChanged();
    }

    public void addData(List<CommentResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CommentChildViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentChildBinding binding;

        public CommentChildViewHolder(@NonNull ItemCommentChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}