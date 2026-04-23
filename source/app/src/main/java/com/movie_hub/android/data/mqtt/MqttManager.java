package com.movie_hub.android.data.mqtt;

import android.content.Context;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import timber.log.Timber;

public class MqttManager {
    private MqttAndroidClient mqttClient;

    public void init(Context context, String serverUri, String clientId, MqttCallbackExtended callback) {
        mqttClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttClient.setCallback(callback);
    }

    public void connect(MqttConnectOptions options) throws MqttException {
        mqttClient.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Timber.d("MQTT Connected");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Timber.e("MQTT Connect Failed: %s", exception.getMessage());
            }
        });
    }

    public void subscribe(String topic, int qos) throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.subscribe(topic, qos);
        }
    }

    public void publish(String topic, String payload, int qos) throws MqttException {
        if (mqttClient.isConnected()) {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            mqttClient.publish(topic, message);
        }
    }

    public void disconnect() throws MqttException {
        if (mqttClient != null) mqttClient.disconnect();
    }
}
