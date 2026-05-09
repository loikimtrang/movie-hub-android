package com.movie_hub.android.ui.main.live.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.databinding.ItemRoomBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private final List<RoomResponse> items = new ArrayList<>();
    private OnRoomClickListener listener;
    private final Context context;
    private boolean myRoomListMode;

    public interface OnRoomClickListener {
        void onRoomClick(RoomResponse model, int position);

        void onRoomDelete(RoomResponse model, int position);
    }

    public void setMyRoomListMode(boolean myRoomListMode) {
        this.myRoomListMode = myRoomListMode;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
        RoomResponse item = items.get(position);

        SwipeLayout swipeLayout = holder.binding.swipeLayout;
        swipeLayout.close(false);

        boolean canSwipeDelete = myRoomListMode && (Objects.equals(item.getState(), Constants.STATE_END) || Objects.equals(item.getState(), Constants.STATE_LOCKED));
        swipeLayout.setSwipeEnabled(canSwipeDelete);
        holder.binding.layoutDelete.setOnClickListener(null);
        if (canSwipeDelete) {
            holder.binding.layoutDelete.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || listener == null) return;
                swipeLayout.close(true);
                swipeLayout.postDelayed(() -> {
                    int updatedPos = holder.getBindingAdapterPosition();
                    if (updatedPos == RecyclerView.NO_POSITION || listener == null) return;
                    listener.onRoomDelete(items.get(updatedPos), updatedPos);
                }, 180);
            });
        }

        holder.binding.tvTitle.setText(item.getName());
        holder.binding.tvNameAuthor.setText(item.getHost().getFullName());
        holder.binding.tvTime.setText(DisplayUtils.getTimeAgo(context, item.getCreatedDate()));

        if (item.getMovieItem().getMovie().getType() == Constants.TYPE_MOVIE_SINGLE) {
            holder.binding.tvSubTitle.setText(item.getMovieItem().getMovie().getTitle());
        } else {
            String title = item.getMovieItem().getMovie().getTitle();
            String episode = context.getString(R.string.episode_char)
                    + item.getMovieItem().getLabel();

            holder.binding.tvSubTitle.setText(episode + " - " + title);
        }

        Glide.with(context)
                .load(Constants.MEDIA_URL + item.getHost().getAvatarPath())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.binding.imgAvatar);

        Glide.with(context)
                .load(item.getMovieItem().getMovie().getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.swipeLayout.setOnClickListener(null);
        holder.binding.layoutRoomSurface.setOnClickListener(v -> {
            if (listener == null) return;
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (swipeLayout.getOpenStatus() != SwipeLayout.Status.Close) {
                swipeLayout.close(true);
                return;
            }
            listener.onRoomClick(items.get(pos), pos);
        });

        holder.binding.icHourglass.clearAnimation();
        holder.binding.icDotLive.clearAnimation();

        if (Objects.equals(item.getState(), Constants.ROOM_STATE_PENDING)) {
            holder.binding.dayStart.setText(context.getString(R.string.premiered_on) + " " + DisplayUtils.formatDateTime(item.getStartTime()));
            holder.binding.dayStart.setVisibility(View.VISIBLE);
            holder.binding.layoutIcLive.setVisibility(View.GONE);
            holder.binding.layoutIcEnd.setVisibility(View.GONE);
            holder.binding.layoutIcWaiting.setVisibility(View.VISIBLE);

            android.view.animation.Animation rotate = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate_center);
            holder.binding.icHourglass.startAnimation(rotate);

        } else if (Objects.equals(item.getState(), Constants.ROOM_STATE_RUNNING)) {
            holder.binding.dayStart.setVisibility(View.GONE);
            holder.binding.layoutIcEnd.setVisibility(View.GONE);
            holder.binding.layoutIcWaiting.setVisibility(View.GONE);
            holder.binding.layoutIcLive.setVisibility(View.VISIBLE);

            android.view.animation.Animation blink = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_blink);
            holder.binding.icDotLive.startAnimation(blink);
        } else {
            holder.binding.dayStart.setText(context.getString(R.string.status_ended));
            holder.binding.dayStart.setVisibility(View.VISIBLE);
            holder.binding.layoutIcWaiting.setVisibility(View.GONE);
            holder.binding.layoutIcLive.setVisibility(View.GONE);
            holder.binding.layoutIcEnd.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<RoomResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addData(List<RoomResponse> newData) {
        if (newData == null || newData.isEmpty()) return;
        int start = items.size();
        items.addAll(newData);
        notifyItemRangeInserted(start, newData.size());
    }

    public RoomResponse getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size() - position);
    }

    public void restoreAt(int position, RoomResponse item) {
        if (item == null) return;
        if (position < 0) position = 0;
        if (position > items.size()) position = items.size();
        items.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, items.size() - position);
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