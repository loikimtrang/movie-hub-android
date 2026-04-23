package com.movie_hub.android.ui.main.home.notification.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.databinding.ItemNotificationBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationResponse> items = new ArrayList<>();
    private final OnNotificationClickListener listener;
    private final Context context;
    private int lastPosition = -1;

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

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NotificationResponse item = items.get(position);
        holder.binding.setItem(item);

        if (!item.isRead()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.tvTitle.setTextAppearance(R.style.Text_12_Bold);
            }
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.tvTime.setTextAppearance(R.style.Text_10_Bold);
            }
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.text));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.tvBody.setTextAppearance(R.style.Text_10_Bold);
            }
            holder.binding.tvBody.setTextColor(ContextCompat.getColor(context, R.color.text));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.tvTitle.setTextAppearance(R.style.Text_12_Normal);
            }
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.tvTime.setTextAppearance(R.style.Text_10_Normal);
            }
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.text_gray));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.tvBody.setTextAppearance(R.style.Text_10_Normal);
            }
            holder.binding.tvBody.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });

        holder.binding.executePendingBindings();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull NotificationViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
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
        int startPos = this.items.size();
        this.items.addAll(newList);
        notifyItemRangeInserted(startPos, newList.size());
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
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