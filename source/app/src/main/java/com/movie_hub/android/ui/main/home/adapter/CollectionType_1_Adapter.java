package com.movie_hub.android.ui.main.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemCollectionType1Binding;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;

import java.util.ArrayList;
import java.util.List;

public class CollectionType_1_Adapter extends RecyclerView.Adapter<CollectionType_1_Adapter.CollectionType_1_ViewHolder> {

    private final List<MovieResponse> items = new ArrayList<>();
    private OnMovieClickCallback listener;
    private Context context;

    public CollectionType_1_Adapter(OnMovieClickCallback listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CollectionType_1_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCollectionType1Binding binding = ItemCollectionType1Binding.inflate(inflater, parent, false);
        return new CollectionType_1_ViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull CollectionType_1_ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieResponse item = items.get(position);

        int iconIndex = position + 1;
        String iconName = "ic_" + iconIndex;

        int resId = context.getResources().getIdentifier(
                iconName,
                "drawable",
                context.getPackageName()
        );

        if (resId != 0) {
            holder.binding.icNumber.setImageResource(resId);
        } else {
            holder.binding.icNumber.setImageResource(R.drawable.ic_1);
        }


        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener == null) return;
            listener.onMovieClick(item);
        });

        holder.binding.getRoot().setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onMovieLongClick(item);
            }
            return true;
        });
        
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_right);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }

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
    public boolean isNumericLabel(String label) {
        if (label == null || label.isEmpty()) return false;
        return label.matches("\\d+");
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, items.size());
        }
    }

    @SuppressLint("NewApi")
    public void setData(List<MovieResponse> newData) {
        items.clear();

        if (newData != null) {
            items.addAll(newData);
        }

        notifyDataSetChanged();
    }

    public void addData(List<MovieResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CollectionType_1_ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCollectionType1Binding binding;

        public CollectionType_1_ViewHolder(@NonNull ItemCollectionType1Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}