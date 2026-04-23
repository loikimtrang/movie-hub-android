package com.movie_hub.android.ui.main.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.databinding.ItemBannerBinding;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;

import java.util.ArrayList;
import java.util.List;

public class MovieBannerAdapter extends RecyclerView.Adapter<MovieBannerAdapter.MovieBannerViewHolder> {

    private final List<SidebarResponse> items = new ArrayList<>();
    OnMovieClickCallback movieClickCallback;
    private Context context;

    public MovieBannerAdapter(Context context, OnMovieClickCallback movieClickCallback) {
        super();
        this.context = context;
        this.movieClickCallback = movieClickCallback;
    }

    @NonNull
    @Override
    public MovieBannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBannerBinding binding = ItemBannerBinding.inflate(inflater, parent, false);
        return new MovieBannerViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull MovieBannerViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SidebarResponse item = items.get(position);
        holder.binding.image.setOnClickListener(v ->{
            if (movieClickCallback == null) return;
            movieClickCallback.onMovieClick(item.getMovie());
        });

        holder.binding.image.setOnLongClickListener(v -> {
            if (movieClickCallback != null) {
                movieClickCallback.onMovieLongClick(item.getMovie());
            }
            return true;
        });

        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getMovie().getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);
    }
    public SidebarResponse getItem(int position) {
        return items != null && position < items.size() ? items.get(position) : null;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, items.size());
        }
    }

    @SuppressLint("NewApi")
    public void setData(List<SidebarResponse> newData) {
        items.clear();

        if (newData != null) {
            items.addAll(newData);
        }

        notifyDataSetChanged();
    }

    public void addData(List<SidebarResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MovieBannerViewHolder extends RecyclerView.ViewHolder {
        private final ItemBannerBinding binding;

        public MovieBannerViewHolder(@NonNull ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}