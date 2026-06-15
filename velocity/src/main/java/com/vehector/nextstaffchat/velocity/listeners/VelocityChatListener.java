package com.vehector.nextstaffchat.velocity.listeners;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

/**
 * Intercepts chat on the proxy so /sc toggle mode and the configurable chat
 * prefix work even when the Paper companion plugin is not installed on the
 * backend.
 *
 * NOTE: On Minecraft 1.19.1+ chat is cryptographically signed. Velocity can
 * still observe the event and request the message to be denied, but cancelling
 * signed chat from a proxy has limited reach on modern clients. For absolute
 * reliability, players should use the /sc command. The toggle/prefix path here
 * works perfectly for unsigned chat and for command-style messages.
 */
public final class VelocityChatListener {

    private final NextStaffChatVelocity plugin;

    public VelocityChatListener(NextStaffChatVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onChat(PlayerChatEvent event) {
        if (!plugin.getConfigManager().isChatInterceptEnabled()) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("nextstaffchat.use")) return;

        String message = event.getMessage();
        String prefix = plugin.getConfigManager().chatPrefix();
        boolean toggled = plugin.getToggleManager().isToggled(player.getUniqueId());
        boolean usingPrefix = prefix != null && !prefix.isEmpty() && message.startsWith(prefix);

        if (!toggled && !usingPrefix) return;

        // Strip prefix if used
        String payload = usingPrefix ? message.substring(prefix.length()).trim() : message;
        if (payload.isEmpty()) return;

        // Try to swallow the original chat so it doesn't leak to public chat.
        try {
            event.setResult(PlayerChatEvent.ChatResult.denied());
        } catch (Throwable ignored) {
            // Older / signed-chat builds may not allow cancellation – best effort.
        }

        String server = player.getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("proxy");
        plugin.getChatService().handleIncoming(
                ChannelConstants.SUB_CHAT,
                player.getUsername(),
                player.getUniqueId().toString(),
                server,
                payload,
                "",
                null);
    }
}
