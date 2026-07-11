package com.movie_hub.android.ui.main.movie.detail.comment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ItemEpisodeCommentBinding;
import com.movie_hub.android.databinding.ItemEpisodeCommentBinding;
import com.movie_hub.android.ui.main.movie.detail.comment.model.TagComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EpisodeCommentItemAdapter extends RecyclerView.Adapter<EpisodeCommentItemAdapter.EpisodeCommentItemViewHolder> {

    private final List<TagComment> items = new ArrayList<>();
    private OnEpisodeCommentClickListener listener;
    private Context context;
    public interface OnEpisodeCommentClickListener {
        void onEpisodeClick(TagComment tagComment);
    }
    public EpisodeCommentItemAdapter(OnEpisodeCommentClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<TagComment> newData) {
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
    public EpisodeCommentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEpisodeCommentBinding binding = ItemEpisodeCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EpisodeCommentItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EpisodeCommentItemViewHolder holder, int position) {
        TagComment item = items.get(position);
        holder.binding.tvEpisode.setText(item.getLabel());

        if (item.isSelect()) {
            holder.binding.content.setSelected(true);
            holder.binding.tvEpisode.setTextColor(
                    ContextCompat.getColor(context, R.color.black));
        } else {
            holder.binding.content.setSelected(false);
            holder.binding.tvEpisode.setTextColor(
                    ContextCompat.getColor(context, R.color.white));
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                if (!item.isSelect()) {
                    for (TagComment s: items) {
                        if (Objects.equals(s.getMovieItemId(), item.getMovieItemId())) {
                            s.setSelect(true);
                            item.setSelect(true);
                        } else {
                            s.setSelect(false);
                        }
                    }
                    notifyDataSetChanged();
                    listener.onEpisodeClick(item);
                }
            }
        });

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.binding.getRoot().getLayoutParams();

        int margin = (int) context.getResources().getDimension(R.dimen._6sdp);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        if (position == 0) {
            layoutParams.setMarginStart(margin * 2);
        }
        if (position == items.size() - 1) {
            layoutParams.setMarginEnd(margin * 2);
        }

        holder.binding.getRoot().setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EpisodeCommentItemViewHolder extends RecyclerView.ViewHolder {
        final ItemEpisodeCommentBinding binding;

        public EpisodeCommentItemViewHolder(ItemEpisodeCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}