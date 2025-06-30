package com.movie_hub.android.helper;

import android.app.Activity;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

public class BlurEffectManager {
    private static int activeBottomSheetCount = 0;

    /**
     * Thêm blur effect khi có bottom sheet mới được hiển thị
     * @param activity Activity hiện tại
     */
    public static void addBlurEffect(Activity activity) {
        activeBottomSheetCount++;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activeBottomSheetCount == 1) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP));
        }
    }

    /**
     * Xóa blur effect khi bottom sheet bị đóng
     * @param activity Activity hiện tại
     */
    public static void removeBlurEffect(Activity activity) {
        activeBottomSheetCount--;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activeBottomSheetCount <= 0) {
            activeBottomSheetCount = 0; // Reset về 0 để tránh số âm
            View decorView = activity.getWindow().getDecorView();
            decorView.setRenderEffect(null);
        }
    }

    /**
     * Reset counter (dùng khi cần thiết reset trạng thái)
     */
    public static void reset() {
        activeBottomSheetCount = 0;
    }

    /**
     * Lấy số lượng bottom sheet đang active (cho debug)
     */
    public static int getActiveCount() {
        return activeBottomSheetCount;
    }
}

