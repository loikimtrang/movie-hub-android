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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.databinding.ItemCommentParentBinding;
import com.movie_hub.android.databinding.ItemReviewBinding;
import com.movie_hub.android.ui.main.movie.detail.comment.model.TagComment;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.ReportPopupUtils;
import com.movie_hub.android.utils.ToxicTextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentParentAdapter extends RecyclerView.Adapter<CommentParentAdapter.CommentParentViewHolder> {

    private final List<CommentResponse> items = new ArrayList<>();
    private OnCommentParentClickListener listener;
    private Context context;
    private TagComment tagComment;
    private Long currentUserId;

    public interface OnCommentParentClickListener {
        void onOpenChildClick(CommentResponse commentResponse, List<CommentResponse> items);
        void onDisLikeClick(CommentResponse commentResponse);
        void onLikeClick(CommentResponse commentResponse);
        void onReplyClick(CommentResponse commentResponse);
        void onReportClick(CommentResponse commentResponse);
        void onEditClick(CommentResponse commentResponse);
        void onDeleteClick(CommentResponse commentResponse);
    }

    public CommentParentAdapter(OnCommentParentClickListener listener, Context context, Long currentUserId) {
        super();
        this.listener = listener;
        this.context = context;
        this.currentUserId = currentUserId;

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
        return new CommentParentViewHolder(binding, currentUserId);
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
        holder.binding.tvNameAuthor.setSelected(true);
        if (tagComment.getLabel() != null && !tagComment.getLabel().isEmpty()) {
            if (tagComment.getMovieItemId() != -1L) {
                holder.binding.tvEpisode.setText(tagComment.getLabel());
                holder.binding.layoutTagEpisode.setVisibility(View.VISIBLE);
            } else {
                if (item.getMovieItem() != null && item.getMovieItem().getParent() != null) {
                    String label = context.getString(R.string.season_char)
                            + item.getMovieItem().getParent().getLabel() + ":"
                            + context.getString(R.string.episode_char)
                            + item.getMovieItem().getLabel();

                    holder.binding.tvEpisode.setText(label);
                    holder.binding.layoutTagEpisode.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.layoutTagEpisode.setVisibility(View.GONE);
                }

            }
        }
        if (item.getAuthor().getGender() == Constants.GENDER_MALE) {
            holder.binding.icGender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gender_male));
        } else if (item.getAuthor().getGender() == Constants.GENDER_FEMALE) {
            holder.binding.icGender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gender_female));
        } else {
            holder.binding.icGender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gender_un));
        }
        holder.binding.tvNameAuthor.setText(DisplayUtils.getAuthorDisplayName(
                context,
                item.getAuthor().getFullName(),
                item.getAuthor().getId(),
                currentUserId
        ));

        boolean needDisplayButton = ToxicTextUtils.bindToxicContent(
                holder.binding.tvContent,
                item.getContent(),
                item.getToxicSpans(),
                item.getStatus(),
                item.isDisplay(),
                0
        );

        holder.binding.tvCountLike.setText(String.valueOf(item.getTotalLike()));
        holder.binding.tvCountDisLike.setText(String.valueOf(item.getTotalDislike()));

        holder.binding.tvTime.setText(DisplayUtils.getTimeAgo(context, item.getCreatedDate()));

        boolean ownContent = isOwnContent(item);
        holder.binding.btnMore.setVisibility(View.VISIBLE);

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
            if (item.getTotalChildren() == 1) {
                holder.binding.tvOpenChildComment.setText(item.getTotalChildren() + " " + context.getString(R.string.comment_non_up_one));
            } else {
                holder.binding.tvOpenChildComment.setText(item.getTotalChildren() + " " + context.getString(R.string.comment_non_up));
            }
            holder.binding.btnOpenComment.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnOpenComment.setVisibility(View.GONE);
        }

        if (Boolean.TRUE.equals(item.getIsOpenChildComment())) {
            holder.binding.icOpenChildComment.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_child_comment));
            if (item.getChildComments() != null && holder.childAdapter.getItemCount() != item.getChildComments().size()) {
                holder.childAdapter.setItems(item.getChildComments());
            }
            holder.binding.childComment.setVisibility(View.VISIBLE);
        } else {
            holder.binding.childComment.setVisibility(View.GONE);
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

            // Reset dislike nếu đang dislike
            if (wasDisliked) {
                item.setDislike(false);
                item.setTotalDislike(item.getTotalDislike() - 1);
            }

            // Toggle like
            item.setLike(!wasLiked);

            // Cập nhật tổng like
            if (item.isLike()) {
                item.setTotalLike(item.getTotalLike() + 1);
            } else {
                item.setTotalLike(item.getTotalLike() - 1);
            }

            listener.onLikeClick(item);
        });

        holder.binding.btnDisLike.setOnClickListener(v -> {
            if (listener == null) return;

            boolean wasLiked = item.isLike();
            boolean wasDisliked = item.isDislike();

            // Reset like nếu đang like
            if (wasLiked) {
                item.setLike(false);
                item.setTotalLike(item.getTotalLike() - 1);
            }

            // Toggle dislike
            item.setDislike(!wasDisliked);

            // Cập nhật tổng dislike
            if (item.isDislike()) {
                item.setTotalDislike(item.getTotalDislike() + 1);
            } else {
                item.setTotalDislike(item.getTotalDislike() - 1);
            }

            listener.onDisLikeClick(item);
        });


        holder.binding.btnReply.setOnClickListener(v -> {
            if (listener == null) return;

            listener.onReplyClick(item);
        });

        holder.binding.btnMore.setOnClickListener(v ->
                ReportPopupUtils.showCommentMorePopup(
                        context,
                        v,
                        ownContent,
                        () -> {
                            if (listener != null) {
                                listener.onReportClick(item);
                            }
                        },
                        () -> {
                            if (listener != null) {
                                listener.onEditClick(item);
                            }
                        },
                        () -> {
                            if (listener != null) {
                                listener.onDeleteClick(item);
                            }
                        }
                )
        );

        holder.binding.btnOpenComment.setOnClickListener(v -> {
            if (listener == null) return;
            item.setIsOpenChildComment(!Boolean.TRUE.equals(item.getIsOpenChildComment()));
            listener.onOpenChildClick(item, items);
            notifyItemChanged(position);
        });

        holder.binding.icDisplay.setVisibility(needDisplayButton ? View.VISIBLE : View.GONE);
        ToxicTextUtils.updateRevealIcon(item.isDisplay(), holder.binding.icDisplay, R.drawable.ic_eye_hidden, R.drawable.ic_eye);

        holder.binding.btnDisplay.setOnClickListener(v -> {
            if (needDisplayButton) {
                item.setDisplay(!item.isDisplay());
                ToxicTextUtils.bindToxicContent(
                        holder.binding.tvContent,
                        item.getContent(),
                        item.getToxicSpans(),
                        item.getStatus(),
                        item.isDisplay(),
                        0
                );
                ToxicTextUtils.updateRevealIcon(item.isDisplay(), holder.binding.icDisplay, R.drawable.ic_eye_hidden, R.drawable.ic_eye);
            }
        });
    }

    public void notifyParentCommentChanged(long commentId, List<CommentResponse> childComment) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == commentId) {
                if (items.get(i).getChildComments() != null) {
                    items.get(i).getChildComments().clear();
                }
                items.get(i).setChildComments(childComment);
                notifyItemChanged(i);
                break;
            }
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
    public void setData(List<CommentResponse> newData, List<VoteListResponse> voteList, TagComment tag) {
        List<CommentResponse> newListClone = new ArrayList<>(newData);
        this.tagComment = tag;

        if (voteList != null) {
            newListClone = handleVoteList(newListClone, voteList);
        }
        items.clear();

        if (newListClone != null) {
            items.addAll(newListClone);
        }

        notifyDataSetChanged();
    }

    public void clearData() {
        items.clear();
        notifyDataSetChanged();
    }

    public List<CommentResponse> handleVoteList(List<CommentResponse> newData, List<VoteListResponse> voteList) {
        if (newData == null) return new ArrayList<>();

        Map<Long, Integer> voteMap = new HashMap<>();
        for (VoteListResponse vote : voteList) {
            voteMap.put(vote.getId(), vote.getType());
        }

        for (CommentResponse comment : newData) {
            int type = voteMap.containsKey(comment.getId()) ? voteMap.get(comment.getId()) : -1;

            if (type == Constants.REACTION_TYPE_LIKE) {
                comment.setLike(true);
                comment.setDislike(false);
            } else if (type == Constants.REACTION_TYPE_DISLIKE) {
                comment.setLike(false);
                comment.setDislike(true);
            } else {
                comment.setLike(false);
                comment.setDislike(false);
            }

            if (comment.getChildComments() != null) {
                for (CommentResponse child : comment.getChildComments()) {
                    int childType = voteMap.containsKey(child.getId()) ? voteMap.get(child.getId()) : -1;

                    if (childType == Constants.REACTION_TYPE_LIKE) {
                        child.setLike(true);
                        child.setDislike(false);
                    } else if (childType == Constants.REACTION_TYPE_DISLIKE) {
                        child.setLike(false);
                        child.setDislike(true);
                    } else {
                        child.setLike(false);
                        child.setDislike(false);
                    }
                }
            }
        }

        return newData;
    }
    public void addData(List<CommentResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }

    private boolean isOwnContent(CommentResponse item) {
        return item.getAuthor() != null
                && currentUserId != null
                && currentUserId > 0
                && item.getAuthor().getId() == currentUserId;
    }

    @Override
    public void onViewRecycled(@NonNull CommentParentViewHolder holder) {
        RecyclerView childRecycler = holder.binding.childComment;
        if (childRecycler.getAdapter() instanceof CommentChildAdapter) {
            ((CommentChildAdapter) childRecycler.getAdapter()).stopTimeUpdater();
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CommentParentViewHolder extends RecyclerView.ViewHolder {
        final ItemCommentParentBinding binding;
        CommentChildAdapter childAdapter; // giữ lại adapter

        public CommentParentViewHolder(@NonNull ItemCommentParentBinding binding, Long currentUserId) {
            super(binding.getRoot());
            this.binding = binding;

            // Init 1 lần duy nhất tại đây
            childAdapter = new CommentChildAdapter(
                    (CommentChildAdapter.OnCommentChildClickListener) binding.getRoot().getContext(),
                    binding.getRoot().getContext(),
                    currentUserId
            );
            binding.childComment.setAdapter(childAdapter);
            binding.childComment.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        }
    }

}