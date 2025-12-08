package com.movie_hub.android.ui.main.movie.detail.review.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.review.ReviewResponse;
import com.movie_hub.android.databinding.ItemReviewBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewItemAdapter extends RecyclerView.Adapter<ReviewItemAdapter.ReviewItemViewHolder> {

    private final List<ReviewResponse> items = new ArrayList<>();
    private ReviewCallback listener;
    private Context context;
    public interface ReviewCallback {
        void onDislike(ReviewResponse ReviewResponse, int position);
        void onLike(ReviewResponse ReviewResponse, int position);
    }
    public ReviewItemAdapter(ReviewCallback listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;

        timeUpdateHandler.postDelayed(timeUpdateRunnable, 60 * 1000);
    }
    private final Handler timeUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            timeUpdateHandler.postDelayed(this, 60 * 1000);
        }
    };

    public void stopTimeUpdater() {
        timeUpdateHandler.removeCallbacksAndMessages(null);
    }
    @NonNull
    @Override
    public ReviewItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReviewItemViewHolder holder, int position) {
        ReviewResponse item = items.get(position);
        holder.binding.tvContent.setText(item.getContent());
        holder.binding.tvCountLike.setText(item.getTotalLike().toString());
        holder.binding.tvCountDisLike.setText(item.getTotalDislike().toString());
        holder.binding.tvNameAuthor.setText(item.getAuthor().getFullName());
        holder.binding.icLike.setImageDrawable(item.isLike() ?
                ContextCompat.getDrawable(context, R.drawable.ic_like_select) :
                ContextCompat.getDrawable(context, R.drawable.ic_like));
        holder.binding.tvTime.setText(DisplayUtils.getTimeAgo(context, item.getCreatedDate()));

        holder.binding.icDisLike.setImageDrawable(item.isDislike() ?
                ContextCompat.getDrawable(context, R.drawable.ic_dislike_select) :
                ContextCompat.getDrawable(context, R.drawable.ic_dislike));

        if (item.getAuthor().getGender() == Constants.GENDER_MALE) {
            holder.binding.icGender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gender_male));
        } else if (item.getAuthor().getGender() == Constants.GENDER_FEMALE) {
            holder.binding.icGender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gender_female));
        } else {
            holder.binding.icGender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gender_un));
        }

        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + item.getAuthor().getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        switch (item.getRate()) {
            case Constants.TYPE_RATING_1:
                holder.binding.icRate.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_1));
                break;

            case Constants.TYPE_RATING_2:
                holder.binding.icRate.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_2));
                break;

            case Constants.TYPE_RATING_3:
                holder.binding.icRate.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_3));
                break;

            case Constants.TYPE_RATING_4:
                holder.binding.icRate.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_4));
                break;

            case Constants.TYPE_RATING_5:
                holder.binding.icRate.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_5));
                break;
            default:
                break;
        }

        holder.binding.btnDisLike.setOnClickListener(view -> {
            if (listener == null) return;

            listener.onDislike(item, position);
        });

        holder.binding.btnLike.setOnClickListener(view -> {
            if (listener == null) return;

            listener.onLike(item, position);
        });

        boolean needDisplayButton = item.getStatus() != null && item.getStatus() == -1;
        applyBlurText(holder.binding, needDisplayButton && !item.isDisplay());

        holder.binding.icDisplay.setVisibility(needDisplayButton ? View.VISIBLE : View.GONE);

        holder.binding.btnDisplay.setOnClickListener(v -> {
            if (item.getStatus() == -1) {
                item.setDisplay(!item.isDisplay());
                applyBlurText(holder.binding, !item.isDisplay());
            }
        });
    }

    private void applyBlurText(ItemReviewBinding binding, boolean blur) {
        Paint paint = binding.tvContent.getPaint();

        if (blur) {
            binding.tvContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            paint.setMaskFilter(new BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL));
            binding.icDisplay.setImageResource(R.drawable.ic_eye_hidden);
        } else {
            paint.setMaskFilter(null);
            binding.icDisplay.setImageResource(R.drawable.ic_eye);
        }

        binding.tvContent.invalidate();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public void setData(List<ReviewResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }
    public void addItemToTop(ReviewResponse review) {
        if (review == null) return;
        items.add(0, review);
        notifyDataSetChanged();
    }

    public void addData(List<ReviewResponse> moreItems) {
        if (moreItems == null || moreItems.isEmpty()) return;
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }

    public void updateItemAt(int position, ReviewResponse updatedReview) {
        if (position >= 0 && position < items.size()) {
            items.set(position, updatedReview);
            notifyItemChanged(position); // Gửi payload nhẹ hơn
        }
    }
    public void updateItemDirectly(RecyclerView recyclerView, int position, ReviewResponse updatedReview) {
        if (position < 0 || position >= items.size()) return;

        items.set(position, updatedReview);

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof ReviewItemAdapter.ReviewItemViewHolder) {
            ReviewItemAdapter.ReviewItemViewHolder holder = (ReviewItemAdapter.ReviewItemViewHolder) viewHolder;

            holder.binding.tvCountLike.setText(String.valueOf(updatedReview.getTotalLike()));
            holder.binding.tvCountDisLike.setText(String.valueOf(updatedReview.getTotalDislike()));

            holder.binding.icLike.setImageDrawable(updatedReview.isLike()
                    ? ContextCompat.getDrawable(context, R.drawable.ic_like_select)
                    : ContextCompat.getDrawable(context, R.drawable.ic_like));

            holder.binding.icDisLike.setImageDrawable(updatedReview.isDislike()
                    ? ContextCompat.getDrawable(context, R.drawable.ic_dislike_select)
                    : ContextCompat.getDrawable(context, R.drawable.ic_dislike));
        }
    }

    public void updateVotesAndRefresh(List<VoteListResponse> voteListResponses) {
        if (voteListResponses == null || voteListResponses.isEmpty()) return;

        for (int i = 0; i < items.size(); i++) {
            ReviewResponse review = items.get(i);

            review.setLike(false);
            review.setDislike(false);

            for (VoteListResponse vote : voteListResponses) {
                if (vote.getId().equals(review.getId())) {
                    if (vote.getType() == Constants.REACTION_TYPE_LIKE) {
                        review.setLike(true);
                    } else if (vote.getType() == Constants.REACTION_TYPE_DISLIKE) {
                        review.setDislike(true);
                    }
                    break;
                }
            }

            notifyItemChanged(i);
        }
    }


    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    static class ReviewItemViewHolder extends RecyclerView.ViewHolder {
        final ItemReviewBinding binding;

        public ReviewItemViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}