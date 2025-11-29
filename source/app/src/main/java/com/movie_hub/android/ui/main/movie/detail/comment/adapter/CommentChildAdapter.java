package com.movie_hub.android.ui.main.movie.detail.comment.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.databinding.ItemCommentChildBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentChildAdapter extends RecyclerView.Adapter<CommentChildAdapter.CommentChildViewHolder> {

    private final Context context;
    private final OnCommentChildClickListener listener;
    private final Handler timeUpdateHandler = new Handler(Looper.getMainLooper());
    private final List<CommentResponse> items = new ArrayList<>();

    public interface OnCommentChildClickListener {
        void onLikeChildClick(CommentResponse commentResponse);
        void onDislikeChildClick(CommentResponse commentResponse);
        void onReplyChildClick(CommentResponse commentResponse);
    }

    public CommentChildAdapter(OnCommentChildClickListener listener, Context context) {
        this.listener = listener;
        this.context = context;
        startTimeUpdater();
    }

    public void startTimeUpdater() {
        timeUpdateHandler.postDelayed(timeUpdateRunnable, 60 * 1000);
    }

    public void stopTimeUpdater() {
        timeUpdateHandler.removeCallbacksAndMessages(null);
    }

    private final Runnable timeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            timeUpdateHandler.postDelayed(this, 60 * 1000);
        }
    };

    public void setItems(List<CommentResponse> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCommentChildBinding binding = ItemCommentChildBinding.inflate(inflater, parent, false);
        return new CommentChildViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentChildViewHolder holder, int position) {
        CommentResponse item = items.get(position);
        if (item == null) return;

        holder.binding.tvNameAuthor.setText(item.getAuthor().getFullName());
        holder.binding.tvContent.setText(item.getContent());
        holder.binding.tvCountLike.setText(String.valueOf(item.getTotalLike()));
        holder.binding.tvCountDisLike.setText(String.valueOf(item.getTotalDislike()));
        holder.binding.tvTime.setText(DisplayUtils.getTimeAgo(context, item.getModifiedDate()));

        Glide.with(holder.itemView.getContext())
                .load(Constants.MEDIA_URL + item.getAuthor().getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.btnReply.setOnClickListener(v -> {
            if (listener != null) listener.onReplyChildClick(item);
        });

        if (!item.isLike() && !item.isDislike()) {
            holder.binding.icLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_like));
            holder.binding.icDisLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_dislike));
        }

        if (item.isLike() && !item.isDislike()) {
            holder.binding.icLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_like_select));
            holder.binding.icDisLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_dislike));
        }

        if (!item.isLike() && item.isDislike()) {
            holder.binding.icLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_like));
            holder.binding.icDisLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_dislike_select));
        }

        holder.binding.btnLike.setOnClickListener(v -> {
            if (listener == null) return;

            boolean wasLiked = item.isLike();
            boolean wasDisliked = item.isDislike();

            if (wasDisliked) {
                item.setDislike(false);
                item.setTotalDislike(item.getTotalDislike() - 1);
            }

            item.setLike(!wasLiked);
            if (item.isLike()) {
                item.setTotalLike(item.getTotalLike() + 1);
            } else {
                item.setTotalLike(item.getTotalLike() - 1);
            }

            listener.onLikeChildClick(item);
            notifyItemChanged(position);
        });

        holder.binding.btnDisLike.setOnClickListener(v -> {
            if (listener == null) return;

            boolean wasLiked = item.isLike();
            boolean wasDisliked = item.isDislike();

            if (wasLiked) {
                item.setLike(false);
                item.setTotalLike(item.getTotalLike() - 1);
            }

            item.setDislike(!wasDisliked);
            if (item.isDislike()) {
                item.setTotalDislike(item.getTotalDislike() + 1);
            } else {
                item.setTotalDislike(item.getTotalDislike() - 1);
            }

            listener.onDislikeChildClick(item);
            notifyItemChanged(position);

        });

        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_bottom);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CommentChildViewHolder extends RecyclerView.ViewHolder {
        final ItemCommentChildBinding binding;

        public CommentChildViewHolder(@NonNull ItemCommentChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
