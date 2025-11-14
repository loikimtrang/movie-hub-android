package com.movie_hub.android.ui.main.movie.detail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.ItemSeasonListBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeasonItemListAdapter extends RecyclerView.Adapter<SeasonItemListAdapter.SeasonItemListViewHolder> {

    private final List<SeasonResponse> seasonList = new ArrayList<>();
    private OnSeasonClickListener listener;
    private Context context;
    public interface OnSeasonClickListener {
        void onSeasonClick(SeasonResponse season);
    }

    public SeasonItemListAdapter(OnSeasonClickListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public SeasonItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSeasonListBinding binding = ItemSeasonListBinding.inflate(inflater, parent, false);
        return new SeasonItemListViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SeasonItemListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SeasonResponse season = seasonList.get(position);
        holder.binding.tvSeason.setText(context.getString(R.string.season) + " " + (position + 1));
        holder.binding.tvYearRelease.setText(DisplayUtils.getYearFromReleaseDate(season.getReleaseDate()));
        holder.binding.tvEpisode.setText(season.getEpisodes().size() + " " + context.getString(R.string.episode_non_up));

        if (position == seasonList.size() - 1) {
            holder.binding.line.setVisibility(View.GONE);
        }

        if (season.isSelect()) {
            holder.binding.icSeason.setImageResource(R.drawable.ic_season_select);
            holder.binding.layoutIcSeason.setSelected(true);
        } else {
            holder.binding.icSeason.setImageResource(R.drawable.ic_season_no_select);
            holder.binding.layoutIcSeason.setSelected(false);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                if (!season.isSelect()) {
                    for (SeasonResponse s: seasonList) {
                        if (Objects.equals(s.getId(), season.getId())) {
                            s.setSelect(true);
                            season.setSelect(true);
                        } else {
                            s.setSelect(false);
                        }
                    }
                    notifyDataSetChanged();
                    listener.onSeasonClick(season);
                }
            }
        });
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull SeasonItemListViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    public void setData(List<SeasonResponse> newData) {
        seasonList.clear();
        if (!newData.isEmpty()) {
            seasonList.addAll(newData);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return seasonList.size();
    }

    static class SeasonItemListViewHolder extends RecyclerView.ViewHolder {
        private final ItemSeasonListBinding binding;

        public SeasonItemListViewHolder(@NonNull ItemSeasonListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}