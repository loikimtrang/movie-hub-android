package com.movie_hub.android.ui.main.movie.watch.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mqtt.CreateChatModel;
import com.movie_hub.android.databinding.ItemChatBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ChatAdapter extends ListAdapter<CreateChatModel, ChatAdapter.ChatViewHolder> {

    private static final long GROUP_TIME_WINDOW_MS = 120_000L;

    private static final DiffUtil.ItemCallback<CreateChatModel> DIFF = new DiffUtil.ItemCallback<CreateChatModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull CreateChatModel oldItem, @NonNull CreateChatModel newItem) {
            return TextUtils.equals(oldItem.getAccountId(), newItem.getAccountId())
                    && TextUtils.equals(oldItem.getCreateDate(), newItem.getCreateDate())
                    && TextUtils.equals(oldItem.getContent(), newItem.getContent());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CreateChatModel oldItem, @NonNull CreateChatModel newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    };

    public ChatAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatBinding binding = ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        CreateChatModel current = getItem(position);
        CreateChatModel previous = position > 0 ? getItem(position - 1) : null;
        holder.bind(current, previous);
    }

    static final class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBinding binding;

        ChatViewHolder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CreateChatModel item, @Nullable CreateChatModel previous) {
            if (item == null) return;
            boolean groupWithPrevious = shouldGroupWithPrevious(item, previous);
            binding.tvContent.setText(item.getContent() != null ? item.getContent() : "");

            if (groupWithPrevious) {
                binding.imgAvatar.setVisibility(View.INVISIBLE);
                binding.layoutInfChat.setVisibility(View.GONE);
            } else {
                binding.imgAvatar.setVisibility(View.VISIBLE);
                binding.layoutInfChat.setVisibility(View.VISIBLE);
                UserResponse author = item.getAuthor();
                String name = "";
                String avatar = "";
                if (author != null) {
                    if (!TextUtils.isEmpty(author.getFullName())) {
                        name = author.getFullName();
                    } else if (!TextUtils.isEmpty(author.getUsername())) {
                        name = author.getUsername();
                    }
                    if (author.getAvatarPath() != null) {
                        avatar = author.getAvatarPath();
                    }
                }
                binding.tvNameAuthor.setText(name);
                binding.tvTime.setText(formatDisplayTime(item.getCreateDate()));
                if (!TextUtils.isEmpty(avatar) && !avatar.contains("http")) {
                    avatar = Constants.MEDIA_URL + avatar;
                }
                Glide.with(binding.imgAvatar.getContext())
                        .load(TextUtils.isEmpty(avatar) ? R.drawable.logo : avatar)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .into(binding.imgAvatar);
            }
        }

        private static boolean shouldGroupWithPrevious(
                @NonNull CreateChatModel current,
                @Nullable CreateChatModel previous) {
            if (previous == null) return false;
            if (!TextUtils.equals(
                    nullToEmpty(current.getAccountId()),
                    nullToEmpty(previous.getAccountId()))) {
                return false;
            }
            long tCur = parseUtcMillis(current.getCreateDate());
            long tPrev = parseUtcMillis(previous.getCreateDate());
            if (tCur < 0L || tPrev < 0L) return false;
            return (tCur - tPrev) < GROUP_TIME_WINDOW_MS;
        }

        private static String nullToEmpty(String s) {
            return s != null ? s : "";
        }

        private static long parseUtcMillis(String createDateStr) {
            if (TextUtils.isEmpty(createDateStr)) return -1L;
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = iso.parse(createDateStr);
                return d != null ? d.getTime() : -1L;
            } catch (Exception ignored) {
                return -1L;
            }
        }

        private static String formatDisplayTime(String createDateStr) {
            if (TextUtils.isEmpty(createDateStr)) return "";
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = iso.parse(createDateStr);
                if (d != null) {
                    return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(d);
                }
            } catch (Exception ignored) {
                // fall through
            }
            return createDateStr;
        }
    }
}
