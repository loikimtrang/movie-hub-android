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
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.ItemMoviePersonBinding;

import java.util.ArrayList;
import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {

    private final List<PersonResponse> personList = new ArrayList<>();
    private OnPersonClickListener listener;

    public interface OnPersonClickListener {
        void onPersonClick(PersonResponse actor);
    }

    public PersonAdapter(OnPersonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMoviePersonBinding binding = ItemMoviePersonBinding.inflate(inflater, parent, false);
        return new PersonViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PersonResponse person = personList.get(position);

        holder.binding.name.setText(person.getName());
        holder.binding.otherName.setText(person.getOtherName());


        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + person.getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onPersonClick(person);
        });
        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_bottom);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, position * 150L);
            lastPosition = position;
        }
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull PersonViewHolder holder) {
        holder.itemView.clearAnimation();
    }
    public void setData(List<PersonResponse> newData) {
        personList.clear();
        if (newData != null) {
            personList.addAll(newData);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return personList.size();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        private final ItemMoviePersonBinding binding;

        public PersonViewHolder(@NonNull ItemMoviePersonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}