package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.subtitle.SubtitleResponse;
import com.movie_hub.android.databinding.LayoutBottomSheetSettingsSubtitleBinding;
import com.movie_hub.android.ui.main.movie.watch.dialog.adapter.SubtitleItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubtitleBottomSheetDialog extends BaseBottomSheetDialog
        implements SubtitleItemAdapter.OnItemClickListener {

    private LayoutBottomSheetSettingsSubtitleBinding binding;
    private final List<SubtitleResponse> subtitles;
    private final SubtitleResponse currentSubtitle;
    private final SubtitleBottomSheetCallback callback;
    private SubtitleItemAdapter adapter;

    public interface SubtitleBottomSheetCallback {
        void onSubtitleSelected(SubtitleResponse subtitle);
    }

    public SubtitleBottomSheetDialog(@NonNull Context context,
                                     List<SubtitleResponse> subtitles,
                                     SubtitleResponse currentSubtitle,
                                     SubtitleBottomSheetCallback callback) {
        super(context);
        this.subtitles = subtitles != null ? subtitles : new ArrayList<>();
        this.currentSubtitle = currentSubtitle;
        this.callback = callback;
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding = LayoutBottomSheetSettingsSubtitleBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setupView();
        binding.layoutSetting.setOnClickListener(v -> dismiss());
    }

    @SuppressLint("SetTextI18n")
    private void setupView() {
        List<SubtitleResponse> itemsWithOff = new ArrayList<>();
        itemsWithOff.add(null); // "Off" entry
        itemsWithOff.addAll(subtitles);

        int selectedPos = 0; // default = Off
        if (currentSubtitle != null) {
            for (int i = 1; i < itemsWithOff.size(); i++) {
                SubtitleResponse sub = itemsWithOff.get(i);
                if (sub != null && Objects.equals(sub.getId(), currentSubtitle.getId())) {
                    selectedPos = i;
                    break;
                }
            }
        }

        String currentLabel = currentSubtitle != null && currentSubtitle.getLabel() != null
                ? currentSubtitle.getLabel()
                : getContext().getString(R.string.off);
        binding.tvCurrentSubtitle.setText(
                getContext().getString(R.string.current_subtitle) + " " + currentLabel);

        adapter = new SubtitleItemAdapter(itemsWithOff, selectedPos, this, getContext());
        binding.rvSubtitle.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSubtitle.setAdapter(adapter);
    }

    @Override
    public void onSubtitleItemClick(SubtitleResponse subtitle, int position) {
        callback.onSubtitleSelected(subtitle); // null means "Off"
        dismiss();
    }
}
