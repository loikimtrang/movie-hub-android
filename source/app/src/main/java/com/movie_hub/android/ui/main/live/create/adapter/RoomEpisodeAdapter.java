package com.movie_hub.android.ui.main.live.create.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.databinding.ItemRoomEpisodeBinding;
import com.movie_hub.android.databinding.ItemRoomEpisodeBinding;
import com.movie_hub.android.ui.main.schedule.adapter.ScheduleModel;
import com.movie_hub.android.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class RoomEpisodeAdapter extends RecyclerView.Adapter<RoomEpisodeAdapter.RoomEpisodeViewHolder> {

    private final List<MovieItemResponse> items = new ArrayList<>();
    private final OnEpisodeClickListener listener;
    private final Context context;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(MovieItemResponse model, int position);
    }

    public RoomEpisodeAdapter(Context context, OnEpisodeClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomEpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomEpisodeBinding binding = ItemRoomEpisodeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RoomEpisodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomEpisodeViewHolder holder, int position) {
        MovieItemResponse item = items.get(position);
        if (item.getVideo() == null) return;

        holder.binding.tvName.setText(context.getString(R.string.episode_index) + " " + item.getLabel());

        if (item.isSelected()) {
            holder.binding.bgItem.setSelected(true);
            holder.binding.tvName.setTextColor(context.getColor(R.color.schedule_time_select));
        } else {
            holder.binding.bgItem.setSelected(false);
            holder.binding.tvName.setTextColor(context.getColor(R.color.text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                updateSelection(position);
                listener.onEpisodeClick(item, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !item.isSelected()) {
                updateSelection(position);
                listener.onEpisodeClick(item, position);
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
    public void setData(List<MovieItemResponse> newData) {
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

    static class RoomEpisodeViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoomEpisodeBinding binding;

        public RoomEpisodeViewHolder(@NonNull ItemRoomEpisodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}