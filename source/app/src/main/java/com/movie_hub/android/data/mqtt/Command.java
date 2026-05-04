package com.movie_hub.android.data.mqtt;

public class Command {
    public static final String COMMAND_CLIENT_PING = "CMD_CLIENT_PING";
    // Client bắn cho host
    public static final String CMD_PARTICIPANT_JOIN = "CMD_PARTICIPANT_JOIN";
    public static final String CMD_ROOM_SYNC = "CMD_ROOM_SYNC";
    public static final String CMD_PARTICIPANT_LEFT = "CMD_PARTICIPANT_LEFT";
    // Host bắn cho client
    public static final String CMD_ROOM_STATE = "CMD_ROOM_STATE"; // Khi Host nhận đc cái CMD_PARTICIPANT_JOIN sẽ gửi
    public static final String CMD_CREATE_CHAT = "CMD_CREATE_CHAT";

    // Sub cmd cho CMD_ROOM_STATE
    public static final String CMD_ROOM_PAUSE = "CMD_ROOM_PAUSE";
    public static final String CMD_ROOM_PLAY = "CMD_ROOM_PLAY";
    public static final String CMD_ROOM_SEEK = "CMD_ROOM_SEEK";
    public static final String CMD_ROOM_PLAY_SPEED = "CMD_ROOM_PLAY_SPEED";
    public static final String CMD_ROOM_ALL_STATE = "CMD_ROOM_ALL_STATE"; // Đồng bộ với host lần đầu tiên

    // Be gui
    public static final String CMD_END_ROOM = "CMD_END_ROOM";

}