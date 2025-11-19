package com.movie_hub.android.ui.main.movie.detail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.databinding.ItemEpisodeBinding;
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

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull EpisodeItemListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieItemResponse item = items.get(position);
        if (item.getVideo() == null) return;

        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvDescription.setText(
                HtmlUtils.convertPtoStrong(item.getDescription()));

        holder.binding.tvDuration.setText(DisplayUtils.displayTimeFromSeconds(context ,item.getVideo().getDuration()));
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getVideo().getThumbnailUrl())
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
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull EpisodeItemListViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    public void setData(List<MovieItemResponse> newData) {
        items.clear();
        if (!newData.isEmpty()) {
            items.addAll(newData);
        }
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