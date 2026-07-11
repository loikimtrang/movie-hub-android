package com.movie_hub.android.ui.main.home.filter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ItemTopHomeFilterBinding;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.utils.ClickUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeTopFilterItemAdapter extends RecyclerView.Adapter<HomeTopFilterItemAdapter.HomeTopFilterItemViewHolder> {

    private final List<FilterTypeModel> items = new ArrayList<>();
    private OnFilterTopClickListener listener;
    private Context context;
    public interface OnFilterTopClickListener {
        void onFilterClick(FilterTypeModel filterTypeModel);
    }
    public HomeTopFilterItemAdapter(OnFilterTopClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<FilterTypeModel> newData) {
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
    public HomeTopFilterItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopHomeFilterBinding binding = ItemTopHomeFilterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeTopFilterItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HomeTopFilterItemViewHolder holder, int position) {
        FilterTypeModel item = items.get(position);
        holder.binding.tvFilter.setText(item.getName());

        if (item.isSelect()) {
            holder.binding.content.setSelected(true);
            holder.binding.tvFilter.setTextColor(
                    ContextCompat.getColor(context, R.color.black));
        } else {
            holder.binding.content.setSelected(false);
            holder.binding.tvFilter.setTextColor(
                    ContextCompat.getColor(context, R.color.white));
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                if (!item.isSelect()) {
                    listener.onFilterClick(item);
                    ClickUtils.debounceClick(holder.binding.getRoot());
                }
            }
        });

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.binding.getRoot().getLayoutParams();

        int margin = (int) context.getResources().getDimension(R.dimen._3sdp);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        if (position == 0) {
            layoutParams.setMarginStart(margin * 4);
        }
        if (position == items.size() - 1) {
            layoutParams.setMarginEnd(margin * 4);
        }

        holder.binding.getRoot().setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HomeTopFilterItemViewHolder extends RecyclerView.ViewHolder {
        final ItemTopHomeFilterBinding binding;

        public HomeTopFilterItemViewHolder(ItemTopHomeFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}