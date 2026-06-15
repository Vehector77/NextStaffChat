package com.vehector.nextstaffchat.velocity.listeners;

import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

/**
 * Standalone-friendly connection events. Detects join/leave/switch directly on
 * the proxy so the plugin works without a Paper companion installed on backend
 * servers.
 */
public final class ConnectionListener {

    private final NextStaffChatVelocity plugin;

    public ConnectionListener(NextStaffChatVelocity plugin) { this.plugin = plugin; }

    @Subscribe(order = PostOrder.LATE)
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer previous = event.getPreviousServer().orElse(null);
        String target = event.getServer().getServerInfo().getName();

        if (previous == null) {
            // First connection to the network -> JOIN
            plugin.getChatService().sendJoin(
                    player.getUsername(),
                    player.getUniqueId().toString(),
                    target);
        } else {
            // Moving between backend servers -> SWITCH
            plugin.getChatService().sendSwitch(
                    player.getUsername(),
                    player.getUniqueId().toString(),
                    previous.getServerInfo().getName(),
                    target);
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        // Only announce when the player was actually playing on the network. If
        // they got booted before reaching a backend server, no join was
        // announced — so don't announce a leave either.
        if (event.getLoginStatus() != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
            return;
        }
        String server = player.getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("");
        plugin.getChatService().sendLeave(
                player.getUsername(),
                player.getUniqueId().toString(),
                server);
    }
}
