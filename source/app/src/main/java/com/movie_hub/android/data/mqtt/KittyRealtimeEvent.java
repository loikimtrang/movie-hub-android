package com.movie_hub.android.data.mqtt;


public interface KittyRealtimeEvent {
    void onMessageReceived(Message message);
    void onConnectionFailed();
    void onConnectionOpened();
    void onConnectionClosed();
    void onMessageTimeout(Message message);
}
