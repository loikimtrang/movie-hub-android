package com.movie_hub.android.ui.main.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.databinding.ItemCollectionTypeTopicBinding;

import java.util.ArrayList;
import java.util.List;

public class CollectionType_Topic_Adapter extends RecyclerView.Adapter<CollectionType_Topic_Adapter.CollectionType_Topic_ViewHolder> {

    private final List<CollectionResponse> items = new ArrayList<>();
    private OnTopicClickCallback listener;
    public interface OnTopicClickCallback {
        void onTopicClick(CollectionResponse collectionResponse);
    }
    private Context context;

    public CollectionType_Topic_Adapter(OnTopicClickCallback listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CollectionType_Topic_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCollectionTypeTopicBinding binding = ItemCollectionTypeTopicBinding.inflate(inflater, parent, false);
        return new CollectionType_Topic_ViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull CollectionType_Topic_ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CollectionResponse item = items.get(position);

        holder.binding.tvTitle.setText(item.getName());
        holder.binding.tvTitle.setSelected(true);

        List<String> colorList = item.getListColor();
        int[] colors = new int[colorList.size()];

        for (int i = 0; i < colorList.size(); i++) {
            try {
                colors[i] = Color.parseColor(colorList.get(i));
            } catch (Exception e) {
                colors[i] = Color.BLACK;
            }
        }

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                colors
        );
        int radius = (int) context.getResources().getDimension(R.dimen._12sdp);
        gradient.setCornerRadius(radius);

        holder.binding.itemContainer.setBackground(gradient);

        holder.binding.itemContainer.setOnClickListener(v -> {
            if (listener == null) return;
            listener.onTopicClick(item);
        });

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
        layoutParams.width = (int) context.getResources().getDimension(R.dimen._140sdp);

        holder.binding.getRoot().setLayoutParams(layoutParams);
    }

    @SuppressLint("NewApi")
    public void setData(List<CollectionResponse> newData) {
        items.clear();

        if (newData != null) {
            items.addAll(newData);
        }

        notifyDataSetChanged();
    }

    public void addData(List<CollectionResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }

    public CollectionResponse getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CollectionType_Topic_ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCollectionTypeTopicBinding binding;

        public CollectionType_Topic_ViewHolder(@NonNull ItemCollectionTypeTopicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}