package com.movie_hub.android;

import static com.movie_hub.android.constant.Constants.TOPIC;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import es.dmoral.toasty.Toasty;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;

import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.onesignal.MessageCommentResponse;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.data.mqtt.Command;
import com.movie_hub.android.data.mqtt.KittyRealtimeEvent;
import com.movie_hub.android.data.mqtt.Message;
import com.movie_hub.android.data.mqtt.MqttManager;
import com.movie_hub.android.data.remote.UploadApiService;
import com.movie_hub.android.di.component.AppComponent;
import com.movie_hub.android.di.component.DaggerAppComponent;
import com.movie_hub.android.others.MyTimberDebugTree;
import com.movie_hub.android.others.MyTimberReleaseTree;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.comment.CommentActivity;
import com.movie_hub.android.ui.main.splash.SplashActivity;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class MVVMApplication extends Application implements LifecycleObserver {
    @Setter
    private AppCompatActivity currentActivity;

    @Getter
    private AppComponent appComponent;
    private Boolean inBackground;
    private static boolean isAppRunning = false;
    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build();
        appComponent.inject(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new MyTimberDebugTree());
        } else {
            Timber.plant(new MyTimberReleaseTree());
        }

        UploadApiService.init(this);
        Toasty.Config.getInstance()
                .allowQueue(false)
                .apply();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        setupOneSignal();
        isAppRunning = true;
    }

    private void setupOneSignal() {
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(Constants.ONESIGNAL_APP_ID);

        OneSignal.setNotificationWillShowInForegroundHandler(notificationReceivedEvent -> {
            OSNotification notification = notificationReceivedEvent.getNotification();

            String title = notification.getTitle();
            String body = notification.getBody();
            JSONObject additionalData = notification.getAdditionalData();
            notificationReceivedEvent.complete(null);

            MessageOneSignal messageOneSignal = GsonUtils.fromJson(GsonUtils.toJson(additionalData), MessageOneSignal.class);
            if (messageOneSignal == null) {
                messageOneSignal = new MessageOneSignal();
            }
            if (messageOneSignal.getTitle() == null || messageOneSignal.getTitle().isEmpty()) {
                messageOneSignal.setTitle(title);
            }
            if (messageOneSignal.getContent() == null || messageOneSignal.getContent().isEmpty()) {
                messageOneSignal.setContent(body);
            }

            showCustomNotification(messageOneSignal);
        });
    }

    private Intent getIntentByCmd(MessageOneSignal message) {
        String json = GsonUtils.toJson(message);
        Intent intent;

        if (!isAppRunning) {
            intent = new Intent(this, SplashActivity.class);
        } else {
            intent = new Intent(this, currentActivity.getClass());

            Intent broadcastIntent = new Intent("NEW_NOTIFICATION_ACTION");
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }

        intent.putExtra("msg_onesignal_data", json);
        intent.putExtra("is_from_notification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
    public void showCustomNotification(MessageOneSignal messageOneSignal) {
        deliverNotificationToCurrentActivity(messageOneSignal);

        String channelId = "movie_hub_notifications";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Movie Hub Updates",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = getIntentByCmd(messageOneSignal);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle(messageOneSignal.getTitle())
                .setContentText(messageOneSignal.getContent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_no_bg);
        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon);
        }

        notificationManager.notify(requestCode, notificationBuilder.build());
    }

    private void deliverNotificationToCurrentActivity(MessageOneSignal messageOneSignal) {
        if (!isAppRunning || currentActivity == null || messageOneSignal == null) {
            return;
        }
        if (currentActivity instanceof CommentActivity
                && OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED.equals(messageOneSignal.getCmd())) {
            ((CommentActivity) currentActivity).onNotificationReceived(messageOneSignal);
        }
    }

    public void setOneSignalExternalId(String userId) {
        if (userId == null || userId.isEmpty()) {
            Timber.e("ONESIGNAL_LOG: UserId bị trống, không thể set External ID");
            return;
        }

        OneSignal.setExternalUserId(userId, new OneSignal.OSExternalUserIdUpdateCompletionHandler() {
            @Override
            public void onSuccess(JSONObject results) {
                Timber.i("ONESIGNAL_LOG: Thiết lập External ID thành công: %s", userId);
                try {
                    if (results != null) {
                        Timber.d("ONESIGNAL_LOG: Kết quả trả về: %s", results.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(OneSignal.ExternalIdError error) {
                Timber.e("ONESIGNAL_LOG: Thiết lập External ID thất bại. Lỗi: %s", error.getMessage());
            }
        });
    }
    public void removeOneSignalExternalId() {
        OneSignal.removeExternalUserId(new OneSignal.OSExternalUserIdUpdateCompletionHandler() {
            @Override
            public void onSuccess(JSONObject results) {
                Timber.i("ONESIGNAL_LOG: Đã xóa External ID thành công.");
            }

            @Override
            public void onFailure(OneSignal.ExternalIdError error) {
                Timber.e("ONESIGNAL_LOG: Xóa External ID thất bại: %s", error.getMessage());
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        //App in background
        Timber.d("APP IN BACKGROUND");
        inBackground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
        Timber.d("APP IN FOREGROUND");
        inBackground = false;
    }


    public PublishSubject<Integer> showDialogNoInternetAccess() {
        final PublishSubject<Integer> subject = PublishSubject.create();
        currentActivity.runOnUiThread(() ->
                DialogUtils.dialogConfirmSingleButton(
                        currentActivity,
                        currentActivity.getString(R.string.newtwork_error),
                        currentActivity.getString(R.string.newtwork_error_button_retry),
                        (dialogInterface, i) -> subject.onNext(1)
                )
        );
        return subject;
    }

    private MqttManager mqttManager;
    private String[] activeMqttTopics;
    private final Map<Integer, Message> pendingRequests = new HashMap<>();
    private final Map<Integer, TimerTask> pendingTimeouts = new HashMap<>();
    private Timer timeoutTimer = new Timer("WebSocketTimeoutTimer");

    @SuppressLint("CheckResult")
    public void createMqtt(String userId, String[] topics) {
        activeMqttTopics = topics;
        // Log danh sách topic để dễ debug
        Timber.d("MQTT_LOG: Initializing MQTT | URL: %s | Topics: %s",
                Constants.MQTT_BROKER, java.util.Arrays.toString(topics));

        mqttManager = new MqttManager();
        mqttManager.init(this, Constants.MQTT_BROKER, userId, new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Timber.i("MQTT_LOG: CONNECTION SUCCESSFUL! Server: %s", serverURI);

                try {
                    int[] qos = new int[topics.length];
                    for (int i = 0; i < topics.length; i++) {
                        qos[i] = 2;
                    }
                    mqttManager.subscribe(topics, qos);

                    Timber.d("MQTT_LOG: Subscribed to all topics successfully");

                    KittyRealtimeEvent event = (KittyRealtimeEvent) currentActivity;
                    if (event != null) event.onConnectionOpened();
                } catch (Exception e) {
                    Timber.e("MQTT_LOG: Subscribe Error: %s", e.getMessage());
                }
            }

            @Override
            public void messageArrived(String topicName, MqttMessage message) {
                if (!isActiveMqttTopic(topicName)) {
                    Timber.w("MQTT_LOG: Ignored message from unsubscribed topic [%s]", topicName);
                    return;
                }
                String payload = new String(message.getPayload());
                Timber.w("MQTT_LOG: MESSAGE FROM [%s] | Payload: %s", topicName, payload);

                try {
                    Message msg = GsonUtils.fromJson(payload, Message.class);
                    int requestId = msg.hashCode();
                    synchronized (pendingTimeouts) {
                        TimerTask task = pendingTimeouts.remove(requestId);
                        if (task != null) task.cancel();
                    }
                    synchronized (pendingRequests) { pendingRequests.remove(requestId); }

                    KittyRealtimeEvent event = (KittyRealtimeEvent) currentActivity;
                    if (event != null) event.onMessageReceived(msg);

                } catch (Exception e) {
                    Timber.e("MQTT_LOG: JSON Parse Error: %s", e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}

            @Override
            public void connectionLost(Throwable cause) {
                Timber.e("MQTT_LOG: CONNECTION LOST!");
                KittyRealtimeEvent event = (KittyRealtimeEvent) currentActivity;
                if (event != null) event.onConnectionClosed();
            }
        });

        // ... Giữ nguyên phần MqttConnectOptions và mqttManager.connect(options) ...
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setUserName(Constants.MQTT_USERNAME);
        options.setPassword(Constants.MQTT_PASSWORD.toCharArray());
        options.setKeepAliveInterval(30);

        try {
            mqttManager.connect(options);
        } catch (MqttException e) {
            Timber.e("MQTT_LOG: Connect failed: %s", e.getMessage());
        }
    }

    public void sendMessageMqtt(Message message, String topic) {
        sendMessageMqtt(message, topic, null);
    }

    public void sendMessageMqtt(Message message, String topic, @Nullable Runnable onDelivered) {
        if (mqttManager != null) {
            try {
                String json = GsonUtils.toJson(message);
                mqttManager.publish(topic, json, 2, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (onDelivered != null) {
                            onDelivered.run();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Timber.e(exception, "MQTT_LOG: Publish failed");
                        if (onDelivered != null) {
                            onDelivered.run();
                        }
                    }
                });
            } catch (MqttException e) {
                Timber.e(e);
                if (onDelivered != null) {
                    onDelivered.run();
                }
            }
        } else if (onDelivered != null) {
            onDelivered.run();
        }
    }

    public void destroyMqtt() {
        Timber.d("MQTT_LOG: Destroying MQTT connection...");

        synchronized (pendingTimeouts) {
            for (TimerTask task : pendingTimeouts.values()) {
                task.cancel();
            }
            pendingTimeouts.clear();
        }

        synchronized (pendingRequests) {
            pendingRequests.clear();
        }

        if (mqttManager != null) {
            try {
                mqttManager.disconnect();
                Timber.i("MQTT_LOG: Disconnected successfully.");
                KittyRealtimeEvent event = (KittyRealtimeEvent) currentActivity;
                if (event != null) event.onConnectionClosed();
            } catch (MqttException e) {
                Timber.e("MQTT_LOG: Disconnect failed: %s", e.getMessage());
            } finally {
                mqttManager = null;
                activeMqttTopics = null;
            }
        } else {
            activeMqttTopics = null;
        }
    }

    private boolean isActiveMqttTopic(String topicName) {
        if (topicName == null || activeMqttTopics == null) {
            return false;
        }
        for (String subscribed : activeMqttTopics) {
            if (topicName.equals(subscribed)) {
                return true;
            }
        }
        return false;
    }
}
