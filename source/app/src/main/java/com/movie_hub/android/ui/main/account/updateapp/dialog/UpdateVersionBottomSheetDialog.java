package com.movie_hub.android.ui.main.account.updateapp.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.LayoutBottomSheetInformationMovieBinding;
import com.movie_hub.android.databinding.LayoutBottomSheetUpdateAppBinding;
import com.movie_hub.android.ui.main.movie.detail.dialog.ChooseSeasonBottomSheetDialog;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.HtmlUtils;

public class UpdateVersionBottomSheetDialog extends BottomSheetDialog  {

    private LayoutBottomSheetUpdateAppBinding binding;
    private CheckAppVersionResponse checkAppVersionResponse;
    private final UpdateVersionBottomSheetCallback callback;

    public interface UpdateVersionBottomSheetCallback {
        void onUpdateClicked();
        void onSkipClicked();
    }
    public UpdateVersionBottomSheetDialog(@NonNull Context context, UpdateVersionBottomSheetCallback callback, CheckAppVersionResponse checkAppVersionResponse) {
        super(context);
        this.callback = callback;
        this.checkAppVersionResponse = checkAppVersionResponse;
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding = LayoutBottomSheetUpdateAppBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        binding.description.setText(checkAppVersionResponse.getLatestVersion().getChangeLog());

        if (checkAppVersionResponse.getForceUpdate()) {
            binding.tvSkip.setText(getContext().getString(R.string.exit));
            binding.status.setVisibility(View.VISIBLE);
        } else {
            binding.tvSkip.setText(getContext().getString(R.string.skip));
        }

        binding.btnUpdate.setOnClickListener(v -> {
            callback.onUpdateClicked();
            dismiss();
        });

        binding.btnSkip.setOnClickListener(v -> {
            callback.onSkipClicked();
            dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        if (!checkAppVersionResponse.getForceUpdate()) {
            super.onBackPressed();
            dismiss();
            callback.onSkipClicked();
        }
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
        window.setNavigationBarColor(Color.TRANSPARENT);


        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        }
    }
}