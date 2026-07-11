package com.movie_hub.android.ui.main.movie.detail.review.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.review.CreateReviewRequest;
import com.movie_hub.android.databinding.ItemReviewTypeBinding;

import java.util.ArrayList;
import java.util.List;

public class ReviewTypeItemAdapter extends RecyclerView.Adapter<ReviewTypeItemAdapter.ReviewTypeItemViewHolder> {

    private final List<CreateReviewRequest> items = new ArrayList<>();
    private RateClickListener listener;
    private Context context;
    public interface RateClickListener {
        void onRateSelect(CreateReviewRequest CreateReviewRequest);
    }
    public ReviewTypeItemAdapter(RateClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<CreateReviewRequest> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewTypeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewTypeBinding binding = ItemReviewTypeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewTypeItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReviewTypeItemViewHolder holder, int position) {
        CreateReviewRequest item = items.get(position);
        switch (item.getRate()) {
            case Constants.TYPE_RATING_1:
                holder.binding.tvReview.setText(context.getString(R.string.terrible));
                holder.binding.icReview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_1));
                break;

            case Constants.TYPE_RATING_2:
                holder.binding.tvReview.setText(context.getString(R.string.boring_movie));
                holder.binding.icReview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_2));
                break;

            case Constants.TYPE_RATING_3:
                holder.binding.tvReview.setText(context.getString(R.string.decent));
                holder.binding.icReview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_3));
                break;

            case Constants.TYPE_RATING_4:
                holder.binding.tvReview.setText(context.getString(R.string.good_movie));
                holder.binding.icReview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_4));
                break;

            case Constants.TYPE_RATING_5:
                holder.binding.tvReview.setText(context.getString(R.string.awesome));
                holder.binding.icReview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_rate_5));
                break;
            default:
                break;
        }

        if (item.isSelect()) {
            holder.binding.layoutContent.setSelected(true);
            holder.binding.icReview.clearColorFilter();
            holder.binding.icReview.setAlpha(1f); // rõ nét
        } else {
            holder.binding.layoutContent.setSelected(false);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); // chuyển về xám
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.binding.icReview.setColorFilter(filter);
            holder.binding.icReview.setAlpha(0.6f); // tối hơn xíu
        }


        holder.binding.getRoot().setOnClickListener(v -> {
            if (!item.isSelect()) {
                for (CreateReviewRequest request : items) {
                    request.setSelect(request.getRate() == item.getRate());
                }
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onRateSelect(item);
                }
            }
        });


        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.binding.getRoot().getLayoutParams();

        int margin = (int) context.getResources().getDimension(R.dimen._6sdp);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        if (position == 0) {
            layoutParams.setMarginStart(margin * 2);
        }
        if (position == items.size() - 1) {
            layoutParams.setMarginEnd(margin * 2);
        }

        holder.binding.getRoot().setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ReviewTypeItemViewHolder extends RecyclerView.ViewHolder {
        final ItemReviewTypeBinding binding;

        public ReviewTypeItemViewHolder(ItemReviewTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}