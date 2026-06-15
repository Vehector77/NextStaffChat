package com.vehector.nextstaffchat.paper.listeners;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinQuitListener implements Listener {

    private final NextStaffChatPaper plugin;

    public JoinQuitListener(NextStaffChatPaper plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getConfigManager().isResetOnSwitch()
                && !plugin.getConfigManager().isTogglePersist()) {
            plugin.getToggleManager().clear(event.getPlayer().getUniqueId());
        }
        if (event.getPlayer().hasPermission("nextstaffchat.use")) {
            plugin.getChatService().sendJoin(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().hasPermission("nextstaffchat.use")) {
            plugin.getChatService().sendLeave(event.getPlayer());
        }
        plugin.getToggleManager().clear(event.getPlayer().getUniqueId());
    }
}
