package com.movie_hub.android.ui.main.live.create.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.databinding.ItemAccountAutocompleteBinding;
import com.movie_hub.android.ui.main.live.create.RoomMemberUiUtils;

import java.util.ArrayList;
import java.util.List;

public class AccountAutoCompleteAdapter extends RecyclerView.Adapter<AccountAutoCompleteAdapter.ViewHolder> {

    public interface OnAccountClickListener {
        void onAccountClick(UserResponse user);
    }

    private final List<UserResponse> items = new ArrayList<>();
    private final Context context;
    private final OnAccountClickListener listener;

    public AccountAutoCompleteAdapter(Context context, OnAccountClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccountAutocompleteBinding binding = ItemAccountAutocompleteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse item = items.get(position);
        String primaryName = RoomMemberUiUtils.getPrimaryName(item);
        String secondaryLabel = RoomMemberUiUtils.getSecondaryLabel(item);

        holder.binding.tvFullName.setText(primaryName);
        if (!TextUtils.isEmpty(secondaryLabel)) {
            holder.binding.tvUsername.setVisibility(View.VISIBLE);
            holder.binding.tvUsername.setText(secondaryLabel);
        } else {
            holder.binding.tvUsername.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(!TextUtils.isEmpty(item.getAvatarPath())
                        ? Constants.MEDIA_URL + item.getAvatarPath()
                        : null)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.binding.imgAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccountClick(item);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<UserResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAccountAutocompleteBinding binding;

        ViewHolder(@NonNull ItemAccountAutocompleteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
