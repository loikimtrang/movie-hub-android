package com.movie_hub.android.ui.main.service;

import android.content.Context;
import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;
import org.json.JSONObject;
import timber.log.Timber;

public class NotificationServiceExtension implements OneSignal.OSRemoteNotificationReceivedHandler {
    @Override
    public void remoteNotificationReceived(Context context, com.onesignal.OSNotificationReceivedEvent notificationReceivedEvent) {
        OSNotification notification = notificationReceivedEvent.getNotification();
        JSONObject additionalData = notification.getAdditionalData();

        Timber.d("ONESIGNAL_LOG: Nhận thông báo từ Background/Service");

        MessageOneSignal msg = new MessageOneSignal();
        msg.setTitle(notification.getTitle());
        msg.setContent(notification.getBody());
        if (additionalData != null) {
            msg.setCmd(additionalData.optString("cmd"));
            msg.setData(additionalData.optString("data"));
        }

        notificationReceivedEvent.complete(null);

        if (context.getApplicationContext() instanceof MVVMApplication) {
            ((MVVMApplication) context.getApplicationContext()).showCustomNotification(msg);
        }
    }
}
