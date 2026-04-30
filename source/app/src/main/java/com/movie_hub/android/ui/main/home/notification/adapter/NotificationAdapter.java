package com.movie_hub.android.ui.main.home.notification.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.databinding.ItemNotificationBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public static final String PAYLOAD_UPDATE_TIME = "PAYLOAD_UPDATE_TIME";
    private final List<NotificationResponse> items = new ArrayList<>();
    private final OnNotificationClickListener listener;
    private final Context context;

    public NotificationResponse getItemAt(int position) {
        return items.get(position);
    }

    public interface OnNotificationClickListener {
        void onItemClick(NotificationResponse item, int position);
    }

    public NotificationAdapter(OnNotificationClickListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(inflater, parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse item = items.get(position);
        holder.binding.setItem(item);

        updateUI(holder, item);
        updateTime(holder, item);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });

        holder.binding.executePendingBindings();
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            for (Object payload : payloads) {
                if (PAYLOAD_UPDATE_TIME.equals(payload)) {
                    updateTime(holder, items.get(position));
                }
            }
        }
    }

    private void updateUI(NotificationViewHolder holder, NotificationResponse item) {
        if (!item.isRead()) {
            holder.binding.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text));
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.binding.icUnRead.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_gray));

            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.binding.icUnRead.setVisibility(View.INVISIBLE);
        }
    }

    private void updateTime(NotificationViewHolder holder, NotificationResponse item) {
        long timestamp = DisplayUtils.parseTimestamp(item.getCreatedDate());
        holder.binding.tvTime.setText(DisplayUtils.getRelativeTime(context, timestamp));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<NotificationResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void addData(List<NotificationResponse> newList) {
        if (newList == null || newList.isEmpty()) return;

        List<NotificationResponse> filteredList = new ArrayList<>();
        for (NotificationResponse newItem : newList) {
            boolean isDuplicate = false;
            for (NotificationResponse existingItem : items) {
                if (existingItem.getId() != null && existingItem.getId().equals(newItem.getId())) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                filteredList.add(newItem);
            }
        }

        if (filteredList.isEmpty()) return;

        int startPos = this.items.size();
        this.items.addAll(filteredList);
        notifyItemRangeInserted(startPos, filteredList.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
