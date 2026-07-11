package com.movie_hub.android.ui.main.home.notification.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ItemTopHomeFilterBinding;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.utils.ClickUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationFilterAdapter extends RecyclerView.Adapter<NotificationFilterAdapter.NotificationFilterTypeViewHolder> {

    private final List<FilterTypeModel> items = new ArrayList<>();
    private final OnFilterTopClickListener listener;

    public interface OnFilterTopClickListener {
        void onFilterClick(FilterTypeModel filterTypeModel);
    }

    public NotificationFilterAdapter(OnFilterTopClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<FilterTypeModel> newData) {
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
    public NotificationFilterTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopHomeFilterBinding binding = ItemTopHomeFilterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationFilterTypeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationFilterTypeViewHolder holder, int position) {
        FilterTypeModel item = items.get(position);
        Context context = holder.itemView.getContext();

        // Bind data
        holder.binding.tvFilter.setText(item.getName());

        // Update UI state
        boolean isSelected = item.isSelect();
        holder.binding.content.setSelected(isSelected);
        holder.binding.tvFilter.setTextColor(ContextCompat.getColor(context,
                isSelected ? R.color.black : R.color.white));

        // Handle Click
        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null && !item.isSelect()) {
                ClickUtils.debounceClick(v); // Thường debounce trước khi gọi callback
                listener.onFilterClick(item);
            }
        });

        // Setup Margins
        setupMargins(holder, position, context);
    }

    private void setupMargins(NotificationFilterTypeViewHolder holder, int position, Context context) {
        if (!(holder.binding.getRoot().getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) return;

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.binding.getRoot().getLayoutParams();
        int margin = (int) context.getResources().getDimension(R.dimen._3sdp);

        int startMargin = (position == 0) ? margin * 4 : margin;
        int endMargin = (position == items.size() - 1) ? margin * 4 : margin;

        layoutParams.setMarginStart(startMargin);
        layoutParams.setMarginEnd(endMargin);
        holder.binding.getRoot().setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationFilterTypeViewHolder extends RecyclerView.ViewHolder {
        final ItemTopHomeFilterBinding binding;

        public NotificationFilterTypeViewHolder(ItemTopHomeFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}