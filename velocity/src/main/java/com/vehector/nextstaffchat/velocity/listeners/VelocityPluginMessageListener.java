package com.vehector.nextstaffchat.velocity.listeners;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class VelocityPluginMessageListener {

    private final NextStaffChatVelocity plugin;

    public VelocityPluginMessageListener(NextStaffChatVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(NextStaffChatVelocity.CHANNEL)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        ServerConnection origin = (event.getSource() instanceof ServerConnection sc) ? sc : null;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()))) {
            String sub = in.readUTF();
            // Ignore our own BROADCAST envelope if it ever loops back.
            if (ChannelConstants.SUB_BROADCAST.equals(sub)) return;
            // JOIN / LEAVE / SWITCH are now detected natively on the proxy, so we
            // drop these to avoid duplicate notifications when both the Velocity
            // and Paper plugins are installed.
            if (ChannelConstants.SUB_JOIN.equals(sub)
                    || ChannelConstants.SUB_LEAVE.equals(sub)
                    || ChannelConstants.SUB_SWITCH.equals(sub)) {
                return;
            }
            String player = in.readUTF();
            String uuid = in.readUTF();
            String server = in.readUTF();
            String message = in.readUTF();
            String gamemode = in.readUTF();
            plugin.getChatService().handleIncoming(sub, player, uuid, server, message, gamemode, origin);
        } catch (IOException e) {
            plugin.getLogger().warn("Bad plugin message: {}", e.getMessage());
        }
    }
}
