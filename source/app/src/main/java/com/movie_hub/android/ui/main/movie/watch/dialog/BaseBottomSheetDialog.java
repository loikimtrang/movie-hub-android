package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.movie_hub.android.R;

public class BaseBottomSheetDialog extends BottomSheetDialog {
    public BaseBottomSheetDialog(@NonNull Context context) {
        super(context, R.style.TransparentBottomSheetDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setupTransparentWindow() {
        Window window = getWindow();
        if (window == null) {
            Log.e("TRANSPARENT", "Window is NULL");
            return;
        }

        Log.d("TRANSPARENT", "Window OK, setting transparent...");

        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // Xử lý hệ thống
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
//                controller.hide(WindowInsetsCompat.Type.statusBars());
//                controller.hide(WindowInsetsCompat.Type.navigationBars());
                Log.d("TRANSPARENT", "Hiding system bars (API 30+)");
            }
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            window.getDecorView().setSystemUiVisibility(flags);
            Log.d("TRANSPARENT", "Set legacy flags");
        }

        // Tìm bottom sheet
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        Log.d("TRANSPARENT", "bottomSheet = " + bottomSheet);

        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            Log.d("TRANSPARENT", "Set bottomSheet background = TRANSPARENT");

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            Log.e("TRANSPARENT", "design_bottom_sheet is NULL! Cannot set transparent background");
        }
    }
}