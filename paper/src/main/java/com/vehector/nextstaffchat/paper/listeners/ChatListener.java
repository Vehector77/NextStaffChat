package com.vehector.nextstaffchat.paper.listeners;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {

    private final NextStaffChatPaper plugin;

    public ChatListener(NextStaffChatPaper plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player p = event.getPlayer();
        if (!plugin.getToggleManager().isToggled(p.getUniqueId())) return;
        if (!p.hasPermission("nextstaffchat.use")) return;
        event.setCancelled(true);
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getChatService().sendChat(p, msg);
    }
}
