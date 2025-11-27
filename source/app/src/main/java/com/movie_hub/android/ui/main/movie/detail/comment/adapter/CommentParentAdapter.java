package com.movie_hub.android.ui.main.movie.detail.comment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.databinding.ItemCommentParentBinding;
import com.movie_hub.android.ui.main.movie.detail.comment.dif.CommentDiffCallback;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentParentAdapter extends RecyclerView.Adapter<CommentParentAdapter.CommentParentViewHolder> {

    private final List<CommentResponse> items = new ArrayList<>();
    private OnCommentParentClickListener listener;
    private Context context;
    public interface OnCommentParentClickListener {
        void onOpenChildClick(CommentResponse commentResponse);
        void onDisLikeClick(CommentResponse commentResponse);
        void onLikeClick(CommentResponse commentResponse);
        void onReplyClick(CommentResponse commentResponse);
    }

    public CommentParentAdapter(OnCommentParentClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;

        timeUpdateHandler.postDelayed(timeUpdateRunnable, 60 * 1000);
    }
    public void stopTimeUpdater() {
        timeUpdateHandler.removeCallbacksAndMessages(null);
    }

    @NonNull
    @Override
    public CommentParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCommentParentBinding binding = ItemCommentParentBinding.inflate(inflater, parent, false);
        return new CommentParentViewHolder(binding);
    }

    private final Handler timeUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            timeUpdateHandler.postDelayed(this, 60 * 1000);
        }
    };

    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull CommentParentViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CommentResponse item = items.get(position);

        holder.binding.tvNameAuthor.setText(item.getAuthor().getFullName());
        holder.binding.tvContent.setText(item.getContent());

        holder.binding.tvCountLike.setText(String.valueOf(item.getTotalLike()));
        holder.binding.tvCountDisLike.setText(String.valueOf(item.getTotalDislike()));

        holder.binding.tvTime.setText(DisplayUtils.getTimeAgo(context, item.getModifiedDate()));

        if (item.isPinned()) {
            holder.binding.icPin.setVisibility(View.VISIBLE);
        } else {
            holder.binding.icPin.setVisibility(View.GONE);
        }

        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + item.getAuthor().getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        if (item.getTotalChildren() > 0) {
            holder.binding.tvOpenChildComment.setText(item.getTotalChildren() + context.getString(R.string.comment_non_up));
            holder.binding.btnOpenComment.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnOpenComment.setVisibility(View.GONE);
        }

        if (item.getIsOpenChildComment()) {
            holder.binding.icOpenChildComment.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_child_comment));
        } else {
            holder.binding.icOpenChildComment.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_open_child_comment));
        }

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
    public void onViewDetachedFromWindow(@NonNull CommentParentViewHolder holder) {
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
    public void updateDataDiff(List<CommentResponse> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentDiffCallback(this.items, newData));

        this.items.clear();
        this.items.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
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

    static class CommentParentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentParentBinding binding;

        public CommentParentViewHolder(@NonNull ItemCommentParentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}