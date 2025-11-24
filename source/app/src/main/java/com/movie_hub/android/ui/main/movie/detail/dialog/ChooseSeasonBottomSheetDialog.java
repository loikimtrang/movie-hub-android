package com.movie_hub.android.ui.main.movie.detail.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.LayoutBottomSheetChooseSeasonBinding;
import com.movie_hub.android.databinding.LayoutBottomSheetSettingsBinding;
import com.movie_hub.android.ui.main.account.adapter.AccountMenuAdapter;
import com.movie_hub.android.ui.main.movie.detail.adapter.SeasonItemListAdapter;
import com.movie_hub.android.ui.main.movie.watch.dialog.BaseBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseSeasonBottomSheetDialog extends BottomSheetDialog implements SeasonItemListAdapter.OnSeasonClickListener  {

    private LayoutBottomSheetChooseSeasonBinding binding;
    private final ChooseSeasonBottomSheetCallback callback;
    public List<SeasonResponse> seasonResponseList;

    public SeasonItemListAdapter adapter;
    public interface ChooseSeasonBottomSheetCallback {
        void onSeasonClicked(SeasonResponse seasonResponse);
    }
    public ChooseSeasonBottomSheetDialog(@NonNull Context context, ChooseSeasonBottomSheetCallback callback, String seasonResponseList) {
        super(context);
        this.callback = callback;
        this.seasonResponseList = GsonUtils.fromJsonToList(seasonResponseList, SeasonResponse.class);
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding = LayoutBottomSheetChooseSeasonBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        adapter = new SeasonItemListAdapter(this, getContext());
        binding.rvSeason.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSeason.setAdapter(adapter);
        adapter.setData(seasonResponseList);

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onSeasonClick(SeasonResponse season) {
        callback.onSeasonClicked(season);
        dismiss();
    }
    @Override
    public void dismiss() {
        super.dismiss();
    }

    @SuppressLint("NewApi")
    public void setupWindow() {
        Window window = getWindow();
        if (window == null) return;

        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(getContext().getColor(R.color.bg_season));


        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        }
    }
}