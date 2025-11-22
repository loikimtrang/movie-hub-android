package com.movie_hub.android.ui.main.movie.watch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.ItemEpisodeBinding;
import com.movie_hub.android.databinding.ItemEpisodeHoriBinding;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EpisodeItemListHoriAdapter extends RecyclerView.Adapter<EpisodeItemListHoriAdapter.EpisodeItemListHoriViewHolder> {

    private final List<MovieItemResponse> items = new ArrayList<>();
    private OnEpisodeClickListener listener;
    private Context context;
    public interface OnEpisodeClickListener {
        void onEpisodeClick(MovieItemResponse season);
    }

    public EpisodeItemListHoriAdapter(OnEpisodeClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public EpisodeItemListHoriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEpisodeHoriBinding binding = ItemEpisodeHoriBinding.inflate(inflater, parent, false);
        return new EpisodeItemListHoriViewHolder(binding);
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull EpisodeItemListHoriViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieItemResponse item = items.get(position);
        if (item.getVideo() == null) return;
        String title = context.getString(R.string.episode_index) + " " + item.getLabel() + "."  + " " + item.getTitle();

        holder.binding.title.setText(title);
        holder.binding.description.setText(
                HtmlUtils.convertPtoStrong(item.getDescription()));

        holder.binding.duration.setText(DisplayUtils.displayTimeFromSeconds(context ,item.getVideo().getDuration()));

        String img = "";

        if (item.getThumbnailUrl() != null) {
            img = item.getThumbnailUrl();
        } else {
            img = item.getVideo().getThumbnailUrl();
        }

        Glide.with(holder.binding.getRoot().getContext())
                .load(img)
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(holder.binding.image);

        if (item.isPlaying()) {
            holder.binding.title.setTypeface(null, Typeface.BOLD);
            holder.binding.play.setVisibility(View.GONE);
        } else  {
            holder.binding.title.setTypeface(null, Typeface.NORMAL);
            holder.binding.play.setVisibility(View.VISIBLE);
        }
        holder.binding.play.setOnClickListener(v -> {
            if (listener != null) {
                if (!item.isPlaying()) {
                    listener.onEpisodeClick(item);
                }
            }
        });
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull EpisodeItemListHoriViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    public void setData(List<MovieItemResponse> newData, RecyclerView recyclerView) {
        items.clear();
        if (newData != null && !newData.isEmpty()) {
            for (MovieItemResponse item : newData) {
                if (item.getVideo() != null) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();

        int playingPosition = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isPlaying()) {
                playingPosition = i;
                break;
            }
        }

        final int finalPosition = playingPosition;
        if (finalPosition != -1 && recyclerView != null) {
            recyclerView.post(() -> recyclerView.smoothScrollToPosition(finalPosition));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EpisodeItemListHoriViewHolder extends RecyclerView.ViewHolder {
        private final ItemEpisodeHoriBinding binding;

        public EpisodeItemListHoriViewHolder(@NonNull ItemEpisodeHoriBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}