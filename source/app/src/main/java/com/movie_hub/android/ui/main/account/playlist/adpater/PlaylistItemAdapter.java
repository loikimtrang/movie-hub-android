package com.movie_hub.android.ui.main.account.playlist.adpater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.databinding.ItemPlayListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistItemAdapter extends RecyclerView.Adapter<PlaylistItemAdapter.PlaylistItemViewHolder> {

    private final List<PlayListResponse> items = new ArrayList<>();
    private OnPlaylistClickListener listener;
    private Context context;
    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlayListResponse playlist);
    }
    public PlaylistItemAdapter(OnPlaylistClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<PlayListResponse> newData) {
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
    public PlaylistItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlayListBinding binding = ItemPlayListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaylistItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlaylistItemViewHolder holder, int position) {
        PlayListResponse item = items.get(position);
        holder.binding.tvName.setText(item.getName());

        if (item.getTotalMovie() > 1) {
            holder.binding.tvCount.setText(item.getTotalMovie() + " " + context.getString(R.string.movie_non_up));
        } else {
            holder.binding.tvCount.setText(item.getTotalMovie() + " " + context.getString(R.string.movie_non_up_one));
        }

        if (item.isSelect()) {
            holder.binding.content.setSelected(true);
            holder.binding.tvName.setTextColor(
                    ContextCompat.getColor(context, R.color.black));
        } else {
            holder.binding.content.setSelected(false);
            holder.binding.tvName.setTextColor(
                    ContextCompat.getColor(context, R.color.text_play_list));
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                if (!item.isSelect()) {
                    for (PlayListResponse s: items) {
                        if (Objects.equals(s.getId(), item.getId())) {
                            s.setSelect(true);
                            item.setSelect(true);
                        } else {
                            s.setSelect(false);
                        }
                    }
                    listener.onPlaylistClick(item);
                    notifyDataSetChanged();
                }
            }
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
    private int lastPosition = -1;

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PlaylistItemViewHolder extends RecyclerView.ViewHolder {
        final ItemPlayListBinding binding;

        public PlaylistItemViewHolder(ItemPlayListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}