package com.movie_hub.android.ui.main.movie.detail.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.ItemMoviePersonBinding;

import java.util.ArrayList;
import java.util.List;
public class MoviePersonAdapter extends RecyclerView.Adapter<MoviePersonAdapter.MoviePersonViewHolder> {

    private final List<MoviePersonResponse> movieList = new ArrayList<>();
    private OnMoviePersonClickListener listener;

    public interface OnMoviePersonClickListener {
        void onActorClick(MoviePersonResponse actor);
    }

    public MoviePersonAdapter(OnMoviePersonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoviePersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMoviePersonBinding binding = ItemMoviePersonBinding.inflate(inflater, parent, false);
        return new MoviePersonViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull MoviePersonViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MoviePersonResponse moviePerson = movieList.get(position);

        holder.binding.name.setText(moviePerson.getPerson().getName());
        holder.binding.otherName.setText(moviePerson.getPerson().getOtherName());


        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + moviePerson.getPerson().getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onActorClick(moviePerson);
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
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull MoviePersonViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    public void setData(List<MoviePersonResponse> newData) {
        movieList.clear();
        if (!newData.isEmpty()) {
            movieList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void addData(List<MoviePersonResponse> moreItems) {
        int startPos = movieList.size();
        movieList.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }
    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MoviePersonViewHolder extends RecyclerView.ViewHolder {
        private final ItemMoviePersonBinding binding;

        public MoviePersonViewHolder(@NonNull ItemMoviePersonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}