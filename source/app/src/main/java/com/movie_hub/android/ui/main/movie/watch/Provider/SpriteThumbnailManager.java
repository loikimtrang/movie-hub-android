package com.movie_hub.android.ui.main.movie.watch.Provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

import com.movie_hub.android.utils.ThumbnailUtils;
import com.movie_hub.android.utils.ThumbnailUtils.ThumbnailInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteThumbnailManager {

    private final Context context;
    private final ImageView previewImageView;
    private final View previewContainer;

    private Bitmap spriteBitmap;
    private final Map<Long, Rect> thumbnailMap = new HashMap<>();

    public SpriteThumbnailManager(Context context, ImageView previewImageView, View previewContainer) {
        this.context = context;
        this.previewImageView = previewImageView;
        this.previewContainer = previewContainer;
    }

    public void load(String vttUrl, String spriteUrl) {
        new Thread(() -> {
            List<ThumbnailInfo> list = ThumbnailUtils.loadVttFile(vttUrl);
            thumbnailMap.clear();
            for (ThumbnailInfo t : list) {
                thumbnailMap.put(t.startMs, new Rect(t.x, t.y, t.x + t.w, t.y + t.h));
            }
            spriteBitmap = ThumbnailUtils.loadSpriteImage(spriteUrl);

            // Log để debug
            // Log.d("THUMB", "Loaded " + list.size() + " thumbnails");
        }).start();
    }

    public void showPreviewAt(long positionMs) {
        if (spriteBitmap == null || thumbnailMap.isEmpty()) return;

        // Tìm key gần nhất bằng tay (thay vì dùng stream)
        long closestStartMs = -1;
        long minDiff = Long.MAX_VALUE;

        for (Long key : thumbnailMap.keySet()) {
            long diff = Math.abs(key - positionMs);
            if (diff < minDiff) {
                minDiff = diff;
                closestStartMs = key;
            }
        }

        Rect rect = thumbnailMap.get(closestStartMs);
        if (rect == null) return;

        try {
            Bitmap thumb = Bitmap.createBitmap(spriteBitmap, rect.left, rect.top, rect.width(), rect.height());
            previewImageView.post(() -> {
                previewImageView.setImageBitmap(thumb);
                previewContainer.setVisibility(View.VISIBLE);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void hidePreview() {
        previewImageView.post(() -> previewContainer.setVisibility(View.GONE));
    }

    public void clear() {
        if (spriteBitmap != null && !spriteBitmap.isRecycled()) {
            spriteBitmap.recycle();
        }
        spriteBitmap = null;
        thumbnailMap.clear();
    }
}