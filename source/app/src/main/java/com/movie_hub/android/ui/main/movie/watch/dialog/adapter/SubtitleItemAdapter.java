package com.movie_hub.android.ui.main.movie.watch.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.subtitle.SubtitleResponse;
import com.movie_hub.android.databinding.ItemSubtitleBinding;

import java.util.List;

public class SubtitleItemAdapter extends RecyclerView.Adapter<SubtitleItemAdapter.SubtitleItemViewHolder> {

    /** Position 0 is always the "Off" entry (null). Positions 1..n are actual subtitles. */
    private final List<SubtitleResponse> items;
    private int selectedPosition;
    private final OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onSubtitleItemClick(SubtitleResponse subtitle, int position);
    }

    public SubtitleItemAdapter(List<SubtitleResponse> items, int selectedPosition,
                               OnItemClickListener listener, Context context) {
        this.items = items;
        this.selectedPosition = selectedPosition;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public SubtitleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubtitleBinding binding = ItemSubtitleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubtitleItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtitleItemViewHolder holder, int position) {
        SubtitleResponse item = items.get(position);
        String label = (item == null || item.getLabel() == null)
                ? (item == null ? context.getString(R.string.off) : item.getLanguage())
                : item.getLabel();
        holder.binding.tvSubtitle.setText(label);
        holder.binding.icCheck.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);

        holder.binding.getRoot().setOnClickListener(v -> {
            int tappedPosition = holder.getAdapterPosition();
            if (tappedPosition == selectedPosition) return; // same item already selected — no-op

            int prev = selectedPosition;
            selectedPosition = tappedPosition;
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onSubtitleItemClick(item, selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SubtitleItemViewHolder extends RecyclerView.ViewHolder {
        ItemSubtitleBinding binding;

        SubtitleItemViewHolder(ItemSubtitleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
