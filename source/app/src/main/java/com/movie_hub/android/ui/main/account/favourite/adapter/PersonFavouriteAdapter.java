package com.movie_hub.android.ui.main.account.favourite.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.ItemMoviePersonBinding;

import java.util.ArrayList;
import java.util.List;

public class PersonFavouriteAdapter extends RecyclerView.Adapter<PersonFavouriteAdapter.PersonFavouriteViewHolder> {

    private final List<FavouriteResponse> favouriteResponses = new ArrayList<>();
    private onPersonFavouriteClickListener listener;

    public interface onPersonFavouriteClickListener {
        void onPersonFavouriteClick(PersonResponse actor);
        void onUnFavouriteClick(FavouriteResponse favouriteResponse);
    }

    public PersonFavouriteAdapter(onPersonFavouriteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonFavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMoviePersonBinding binding = ItemMoviePersonBinding.inflate(inflater, parent, false);
        return new PersonFavouriteViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull PersonFavouriteViewHolder holder, @SuppressLint("RecyclerView") int position) {

        FavouriteResponse favouriteResponse = favouriteResponses.get(position);

        PersonResponse person = favouriteResponses.get(position).getPerson();
        holder.binding.btnUnFavourite.setVisibility(View.VISIBLE);
        
        holder.binding.name.setText(person.getName());
        holder.binding.otherName.setText(person.getOtherName());


        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + person.getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onPersonFavouriteClick(person);
        });

        holder.binding.btnUnFavourite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnFavouriteClick(favouriteResponse);
                removeItem(position);
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
    }

    public void removeItem(int position) {
        if (position >= 0 && position < favouriteResponses.size()) {
            favouriteResponses.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, favouriteResponses.size());
        }
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull PersonFavouriteViewHolder holder) {
        holder.itemView.clearAnimation();
    }
    public void setData(List<FavouriteResponse> newData) {
        favouriteResponses.clear();
        if (newData != null) {
            favouriteResponses.addAll(newData);
        }
        notifyDataSetChanged();
    }
    public void addData(List<FavouriteResponse> moreItems) {
        int startPos = favouriteResponses.size();
        favouriteResponses.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }


    @Override
    public int getItemCount() {
        return favouriteResponses.size();
    }

    static class PersonFavouriteViewHolder extends RecyclerView.ViewHolder {
        private final ItemMoviePersonBinding binding;

        public PersonFavouriteViewHolder(@NonNull ItemMoviePersonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}