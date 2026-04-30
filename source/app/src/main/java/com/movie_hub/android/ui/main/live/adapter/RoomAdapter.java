package com.movie_hub.android.ui.main.live.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.databinding.ItemRoomBinding;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private final List<RoomResponse> items = new ArrayList<>();
    private OnRoomClickListener listener;
    private final Context context;

    public interface OnRoomClickListener {
        void onRoomClick(RoomResponse model, int position);
    }

    public RoomAdapter(Context context, OnRoomClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomAdapter.RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomBinding binding = ItemRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RoomAdapter.RoomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
       
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<RoomResponse> newData) {
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

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoomBinding binding;

        public RoomViewHolder(@NonNull ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}