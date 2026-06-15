package com.vehector.nextstaffchat.paper.listeners;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private final NextStaffChatPaper plugin;

    public PluginMessageListener(NextStaffChatPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!ChannelConstants.CHANNEL.equals(channel)) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String sub = in.readUTF();
            if (ChannelConstants.SUB_BROADCAST.equals(sub)) {
                String permission = in.readUTF();
                String formatted = in.readUTF();
                plugin.getChatService().receiveBroadcast(formatted, permission);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Bad plugin message: " + e.getMessage());
        }
    }
}
