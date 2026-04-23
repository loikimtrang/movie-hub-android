package com.movie_hub.android.ui.main.schedule.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ItemScheduleTimeBinding;
import com.movie_hub.android.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTimeAdapter extends RecyclerView.Adapter<ScheduleTimeAdapter.ScheduleTimeViewHolder> {

    private final List<ScheduleModel> items = new ArrayList<>();
    private final OnScheduleClickListener listener;
    private final Context context;

    public interface OnScheduleClickListener {
        void onDateClick(ScheduleModel model, int position);
    }

    public ScheduleTimeAdapter(Context context, OnScheduleClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleTimeBinding binding = ItemScheduleTimeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ScheduleTimeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleTimeViewHolder holder, int position) {
        ScheduleModel item = items.get(position);

        holder.binding.date1.setText(DateUtils.getDayOfMonth(item.getFullDate()));
        holder.binding.date2.setText(DateUtils.getDayOfWeek(context, item.getFullDate()));

        if (item.isSelected()) {
            holder.binding.bgItem.setSelected(true);
            holder.binding.date1.setTextColor(context.getColor(R.color.schedule_time_select));
            holder.binding.date2.setTextColor(context.getColor(R.color.schedule_time_select));
        } else {
            holder.binding.bgItem.setSelected(false);
            holder.binding.date1.setTextColor(context.getColor(R.color.text_time_schedule));
            holder.binding.date2.setTextColor(context.getColor(R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !item.isSelected()) {
                updateSelection(position);
                listener.onDateClick(item, position);
            }
        });
    }
    private void updateSelection(int position) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ScheduleModel> newData, RecyclerView recyclerView) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ScheduleTimeViewHolder extends RecyclerView.ViewHolder {
        private final ItemScheduleTimeBinding binding;

        public ScheduleTimeViewHolder(@NonNull ItemScheduleTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}