package com.movie_hub.android.ui.main.home.notification.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.databinding.ItemNotificationBinding;
import com.movie_hub.android.ui.main.home.notification.model.NotificationDisplayModel;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.NotificationUiUtils;
import com.movie_hub.android.utils.ToxicTextUtils;

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

        void onNotificationDelete(NotificationResponse item, int position);
    }

    public NotificationAdapter(OnNotificationClickListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse item = items.get(position);
        NotificationDisplayModel display = NotificationUiUtils.build(context, item);

        SwipeLayout swipeLayout = holder.binding.swipeLayout;
        swipeLayout.close(false);
        swipeLayout.setSwipeEnabled(true);

        holder.binding.layoutDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || listener == null) return;
            swipeLayout.close(true);
            swipeLayout.postDelayed(() -> {
                int updatedPos = holder.getBindingAdapterPosition();
                if (updatedPos == RecyclerView.NO_POSITION || listener == null) return;
                listener.onNotificationDelete(items.get(updatedPos), updatedPos);
            }, 180);
        });

        holder.binding.tvTitle.setText(display.getTitle());
        bindOptionalText(holder.binding.tvSubtitle, display.getSubtitle());
        bindPreview(holder, display);
        updateTime(holder, item);
        updateReadState(holder, item);
        bindAvatar(holder, display);

        holder.binding.layoutRoot.setOnClickListener(v -> {
            if (swipeLayout.getOpenStatus() != SwipeLayout.Status.Close) {
                swipeLayout.close(true);
                return;
            }
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                listener.onItemClick(items.get(pos), pos);
            }
        });
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

    private void bindOptionalText(android.widget.TextView textView, String value) {
        if (TextUtils.isEmpty(value)) {
            textView.setVisibility(View.GONE);
            textView.setText(null);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(value);
        }
    }

    private void bindPreview(NotificationViewHolder holder, NotificationDisplayModel display) {
        if (TextUtils.isEmpty(display.getPreview())) {
            holder.binding.tvPreview.setVisibility(View.GONE);
            ToxicTextUtils.clearBlur(holder.binding.tvPreview);
            holder.binding.tvPreview.setText(null);
            return;
        }

        holder.binding.tvPreview.setVisibility(View.VISIBLE);
        if (display.isPreviewMasked()) {
            ToxicTextUtils.bindToxicContent(
                    holder.binding.tvPreview,
                    display.getPreview(),
                    display.getToxicSpans(),
                    display.getToxicStatus(),
                    false,
                    0
            );
        } else {
            ToxicTextUtils.clearBlur(holder.binding.tvPreview);
            holder.binding.tvPreview.setText(display.getPreview());
        }
    }

    private void bindAvatar(NotificationViewHolder holder, NotificationDisplayModel display) {
        holder.binding.imgAvatar.setVisibility(View.VISIBLE);
        if (display.isShowAvatar() && !TextUtils.isEmpty(display.getAvatarUrl())) {
            Glide.with(context)
                    .load(display.getAvatarUrl())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.binding.imgAvatar);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_notification_logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.binding.imgAvatar);
        }
    }

    private void updateReadState(NotificationViewHolder holder, NotificationResponse item) {
        if (!item.isRead()) {
            holder.binding.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text));
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.binding.icUnRead.setVisibility(View.VISIBLE);
            holder.binding.layoutRoot.setAlpha(1f);
        } else {
            holder.binding.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.binding.icUnRead.setVisibility(View.INVISIBLE);
            holder.binding.layoutRoot.setAlpha(0.88f);
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

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size() - position);
    }

    public void restoreAt(int position, NotificationResponse item) {
        if (item == null) return;
        if (position < 0) position = 0;
        if (position > items.size()) position = items.size();
        items.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, items.size() - position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void markAllAsRead() {
        for (NotificationResponse item : items) {
            item.setRead(true);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
