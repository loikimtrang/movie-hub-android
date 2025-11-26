package com.movie_hub.android.ui.main.account.updateapp;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.other.ToastMessage;

import java.io.File;

public class UpdateManager {

    private final Context context;
    private long downloadId;

    public UpdateManager(Context context) {
        this.context = context;
    }

    @SuppressLint("InlinedApi")
    public void downloadApk(String fileUrl, Runnable onDownloadSuccess) {
        File destination = new File(context.getExternalFilesDir(null), "app_update.apk");
        if (destination.exists()) destination.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle(context.getString(R.string.loading_update));
        request.setDescription(context.getString(R.string.app_will_auto));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(destination));

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadId = manager.enqueue(request);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = manager.query(query);

                    if (cursor != null && cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(onDownloadSuccess);
                        } else {
                            new ToastMessage(ToastMessage.TYPE_ERROR, context.getString(R.string.download_failed)).showMessage(context);
                        }
                        cursor.close();
                    }

                    context.unregisterReceiver(this);
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
    }
}