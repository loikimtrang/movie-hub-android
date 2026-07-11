package com.movie_hub.android.ui.main.movie.watch.adapter;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.room.ParticipantDto;

import java.util.ArrayList;
import java.util.List;

public class ParticipantListAdapter extends RecyclerView.Adapter<ParticipantListAdapter.ViewHolder> {

    public interface OnMoreClickListener {
        /** Called when "..." is tapped; {@code anchor} is the button view for popup positioning. */
        void onMoreClick(View anchor, ParticipantDto participant);
    }

    private final List<ParticipantDto> items = new ArrayList<>();
    private final long myUserId;
    private final long hostUserId;
    private final boolean isHost;
    @Nullable private final OnMoreClickListener moreClickListener;

    public ParticipantListAdapter(long myUserId, long hostUserId, boolean isHost,
                                  @Nullable OnMoreClickListener moreClickListener) {
        this.myUserId = myUserId;
        this.hostUserId = hostUserId;
        this.isHost = isHost;
        this.moreClickListener = moreClickListener;
    }

    public void submitList(List<ParticipantDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParticipantDto dto = items.get(position);
        if (dto == null) return;

        long userId = dto.getUser() != null && dto.getUser().getId() != null
                ? dto.getUser().getId() : -1L;
        boolean isMe = userId == myUserId;
        boolean isParticipantHost = userId == hostUserId;

        String displayName = dto.getUser() != null
                ? (!TextUtils.isEmpty(dto.getUser().getFullName())
                    ? dto.getUser().getFullName()
                    : dto.getUser().getUsername())
                : "Unknown";

        StringBuilder name = new StringBuilder(displayName != null ? displayName : "");
        if (isParticipantHost) name.append(" (host)");

        holder.tvName.setText(name.toString());
        // Bold if this is the current user
        holder.tvName.setTypeface(isMe ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        String avatar = dto.getUser() != null ? dto.getUser().getAvatarPath() : null;
        if (!TextUtils.isEmpty(avatar)) {
            if (!avatar.contains("http")) {
                avatar = Constants.MEDIA_URL + avatar;
            }
            Glide.with(holder.ivAvatar.getContext())
                    .load(avatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // "..." only visible to host, and not for the host's own row
        if (isHost && !isMe) {
            holder.btnMore.setVisibility(View.VISIBLE);
            holder.btnMore.setOnClickListener(v -> {
                if (moreClickListener != null) moreClickListener.onMoreClick(v, dto);
            });
        } else {
            holder.btnMore.setVisibility(View.GONE);
            holder.btnMore.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_participant_avatar);
            tvName = itemView.findViewById(R.id.tv_participant_name);
            btnMore = itemView.findViewById(R.id.btn_participant_more);
        }
    }
}
