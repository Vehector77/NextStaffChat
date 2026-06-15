package com.vehector.nextstaffchat.paper.listeners;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public final class GameModeListener implements Listener {

    private final NextStaffChatPaper plugin;

    public GameModeListener(NextStaffChatPaper plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChange(PlayerGameModeChangeEvent event) {
        if (!event.getPlayer().hasPermission("nextstaffchat.use")) return;
        plugin.getChatService().sendGamemode(event.getPlayer(), event.getNewGameMode().name());
    }
}
