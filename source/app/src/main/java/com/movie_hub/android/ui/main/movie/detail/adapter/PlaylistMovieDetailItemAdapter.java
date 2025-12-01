package com.movie_hub.android.ui.main.movie.detail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.databinding.ItemPlayListMovieDetailsBinding;
import com.movie_hub.android.databinding.ItemPlayListMovieDetailsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistMovieDetailItemAdapter extends RecyclerView.Adapter<PlaylistMovieDetailItemAdapter.PlaylistMovieDetailItemViewHolder> {

    private final List<PlayListResponse> items = new ArrayList<>();
    private OnPlaylistClickListener listener;
    private Context context;
    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlayListResponse playlist);
    }
    public PlaylistMovieDetailItemAdapter(OnPlaylistClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<PlayListResponse> newData) {
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
    public PlaylistMovieDetailItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlayListMovieDetailsBinding binding = ItemPlayListMovieDetailsBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaylistMovieDetailItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlaylistMovieDetailItemViewHolder holder, int position) {
        PlayListResponse item = items.get(position);
        holder.binding.tvName.setText(item.getName());
        if (position == items.size() - 1) {
            holder.binding.line.setVisibility(View.GONE);
        }
        if (item.getTotalMovie() > 1) {
            holder.binding.tvCount.setText(item.getTotalMovie() + " " + context.getString(R.string.movie_non_up));
        } else {
            holder.binding.tvCount.setText(item.getTotalMovie() + " " + context.getString(R.string.movie_non_up_one));
        }

        if (item.isSelect()) {
            holder.binding.icCheck.setImageResource(R.drawable.ic_check_box_check);
        } else {
            holder.binding.icCheck.setImageResource(R.drawable.ic_check_box_un_check);

        }

        holder.binding.btnCheck.setOnClickListener(v -> {
            if (listener != null) {
                for (PlayListResponse s: items) {
                    if (Objects.equals(s.getId(), item.getId())) {
                        if (item.isSelect()) {
                            s.setSelect(false);
                            item.setSelect(false);
                        } else {
                            s.setSelect(true);
                            item.setSelect(true);
                        }
                    }
                }
                listener.onPlaylistClick(item);
                notifyItemChanged(position);
            }
        });
    }
    private int lastPosition = -1;

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PlaylistMovieDetailItemViewHolder extends RecyclerView.ViewHolder {
        final ItemPlayListMovieDetailsBinding binding;

        public PlaylistMovieDetailItemViewHolder(ItemPlayListMovieDetailsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}