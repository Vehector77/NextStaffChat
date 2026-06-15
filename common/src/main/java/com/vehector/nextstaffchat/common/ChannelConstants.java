package com.vehector.nextstaffchat.common;

/**
 * Constants and types shared between Paper and Velocity modules.
 * Channel and payload definitions for the Plugin Messaging pipeline.
 */
public final class ChannelConstants {

    private ChannelConstants() {}

    /** Main plugin messaging channel namespace:name. */
    public static final String CHANNEL = "nextstaffchat:main";
    public static final String CHANNEL_NAMESPACE = "nextstaffchat";
    public static final String CHANNEL_NAME = "main";

    /** Sub-channels (first UTF written into the stream). */
    public static final String SUB_CHAT = "CHAT";
    public static final String SUB_JOIN = "JOIN";
    public static final String SUB_LEAVE = "LEAVE";
    public static final String SUB_SWITCH = "SWITCH";
    public static final String SUB_GAMEMODE = "GAMEMODE";
    /** Velocity -> Paper rebroadcast envelope. */
    public static final String SUB_BROADCAST = "BROADCAST";
}
