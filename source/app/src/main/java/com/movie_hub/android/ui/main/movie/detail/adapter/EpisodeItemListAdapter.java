package com.movie_hub.android.ui.main.movie.detail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.databinding.ItemEpisodeBinding;
import com.movie_hub.android.ui.main.movie.detail.diff.EpisodeDiffCallback;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

public class EpisodeItemListAdapter extends RecyclerView.Adapter<EpisodeItemListAdapter.EpisodeItemListViewHolder> {

    private final List<MovieItemResponse> items = new ArrayList<>();
    private OnEpisodeClickListener listener;
    private Context context;
    public interface OnEpisodeClickListener {
        void onEpisodeClick(MovieItemResponse episode);
    }

    public EpisodeItemListAdapter(OnEpisodeClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public EpisodeItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEpisodeBinding binding = ItemEpisodeBinding.inflate(inflater, parent, false);
        return new EpisodeItemListViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull EpisodeItemListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieItemResponse item = items.get(position);
        if (item.getVideo() == null) return;

        String title = item.getLabel() + "."  + " " + item.getTitle();

        holder.binding.tvTitle.setText(title);
        holder.binding.tvDescription.setText(
                HtmlUtils.convertPtoStrong(item.getDescription()));

        holder.binding.tvDuration.setText(DisplayUtils.displayTimeFromSeconds(context ,item.getVideo().getDuration()));
        holder.binding.seekBar.setOnTouchListener((v, event) -> true);
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

        holder.binding.play.setOnClickListener(v -> {
            if (listener != null) {
                if (!item.isPlaying()) {
                    listener.onEpisodeClick(item);
                }
            }
        });

        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_bottom);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }

        if ((item.getLastWatchSeconds() == null || item.getVideo() == null || item.getVideo().getDuration() == null)
                && !item.isCompleted()) {
            holder.binding.seekBar.setVisibility(View.GONE);
        } else {
            holder.binding.seekBar.setVisibility(View.VISIBLE);

            if (item.isCompleted()) {
                holder.binding.seekBar.setMax(100);
                holder.binding.seekBar.setProgress(100);
            } else {
                int duration = item.getVideo().getDuration().intValue();
                int progress = item.getLastWatchSeconds() != null ? item.getLastWatchSeconds().intValue() : 0;

                holder.binding.seekBar.setMax(duration);
                holder.binding.seekBar.setProgress(progress);
            }
        }
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull EpisodeItemListViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    @SuppressLint("NewApi")
    public void setData(List<MovieItemResponse> newDataRaw) {
        List<MovieItemResponse> filtered = new ArrayList<>();

        if (newDataRaw != null) {
            newDataRaw.stream()
                    .filter(item -> item.getVideo() != null)
                    .forEach(filtered::add);
        }

        this.items.clear();
        this.items.addAll(filtered);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EpisodeItemListViewHolder extends RecyclerView.ViewHolder {
        private final ItemEpisodeBinding binding;

        public EpisodeItemListViewHolder(@NonNull ItemEpisodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}