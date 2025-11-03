package com.movie_hub.android.ui.main.search.suggestion.adapter;

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
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.ItemActorBinding;
import java.util.ArrayList;
import java.util.List;
public class ActorAdapter extends RecyclerView.Adapter<ActorAdapter.ActorViewHolder> {

    private final List<PersonResponse> movieList = new ArrayList<>();
    private OnActorClickListener listener;

    public interface OnActorClickListener {
        void onActorClick(PersonResponse actor);
    }

    public ActorAdapter(OnActorClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemActorBinding binding = ItemActorBinding.inflate(inflater, parent, false);
        return new ActorViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull ActorViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PersonResponse actor = movieList.get(position);

        holder.binding.name.setText(actor.getName());

        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + actor.getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onActorClick(actor);
        });
        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_right);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull ActorViewHolder holder) {
        holder.itemView.clearAnimation();
    }
    public void setData(List<PersonResponse> newData) {
        movieList.clear();
        if (newData != null) {
            movieList.addAll(newData);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class ActorViewHolder extends RecyclerView.ViewHolder {
        private final ItemActorBinding binding;

        public ActorViewHolder(@NonNull ItemActorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}