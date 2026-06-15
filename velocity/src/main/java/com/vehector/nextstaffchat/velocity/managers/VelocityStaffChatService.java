package com.vehector.nextstaffchat.velocity.managers;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import com.vehector.nextstaffchat.velocity.utils.VelocityColorUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Routes incoming Paper messages to a formatted broadcast, then sends it
 * back to all backend servers so every Paper instance can deliver it to
 * staff with permission.
 */
public final class VelocityStaffChatService {

    private final NextStaffChatVelocity plugin;

    public VelocityStaffChatService(NextStaffChatVelocity plugin) {
        this.plugin = plugin;
    }

    public void handleIncoming(String sub, String playerName, String uuid,
                               String reportedServer, String message, String gamemode,
                               ServerConnection origin) {
        String template;
        String permission;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", playerName);
        placeholders.put("displayname", playerName);
        placeholders.put("server", origin != null ? origin.getServerInfo().getName() : reportedServer);
        placeholders.put("rank", resolveRank(uuid));
        placeholders.put("message", message);
        placeholders.put("gamemode", gamemode);
        placeholders.put("from", "");
        placeholders.put("to", "");

        switch (sub) {
            case ChannelConstants.SUB_CHAT -> {
                if (!plugin.getConfigManager().isChatEnabled()) return;
                template = plugin.getConfigManager().string("messages.chat-format",
                        plugin.getConfigManager().msg("messages.chat-format"));
                permission = "nextstaffchat.use";
            }
            case ChannelConstants.SUB_JOIN -> {
                if (!plugin.getConfigManager().isJoinEnabled()) return;
                template = plugin.getConfigManager().string("messages.join-format", "");
                permission = "nextstaffchat.notify.join";
            }
            case ChannelConstants.SUB_LEAVE -> {
                if (!plugin.getConfigManager().isLeaveEnabled()) return;
                template = plugin.getConfigManager().string("messages.leave-format", "");
                permission = "nextstaffchat.notify.leave";
            }
            case ChannelConstants.SUB_GAMEMODE -> {
                if (!plugin.getConfigManager().isGamemodeEnabled()) return;
                template = plugin.getConfigManager().string("messages.gamemode-format", "");
                permission = "nextstaffchat.notify.gamemode";
            }
            default -> { return; }
        }

        String formatted = applyPlaceholders(template, placeholders);
        rebroadcast(formatted, permission);
    }

    public void sendSwitch(String playerName, String uuid, String from, String to) {
        if (!plugin.getConfigManager().isSwitchEnabled()) return;
        String template = plugin.getConfigManager().string("messages.switch-format", "");
        Map<String, String> ph = new HashMap<>();
        ph.put("player", playerName);
        ph.put("displayname", playerName);
        ph.put("from", from == null ? "" : from);
        ph.put("to", to == null ? "" : to);
        ph.put("server", to == null ? "" : to);
        ph.put("rank", resolveRank(uuid));
        ph.put("message", "");
        ph.put("gamemode", "");
        String formatted = applyPlaceholders(template, ph);
        rebroadcast(formatted, "nextstaffchat.notify.switch");
    }

    public void sendJoin(String playerName, String uuid, String server) {
        if (!plugin.getConfigManager().isJoinEnabled()) return;
        String template = plugin.getConfigManager().string("messages.join-format", "");
        Map<String, String> ph = new HashMap<>();
        ph.put("player", playerName);
        ph.put("displayname", playerName);
        ph.put("server", server == null ? "" : server);
        ph.put("rank", resolveRank(uuid));
        ph.put("message", "");
        ph.put("gamemode", "");
        ph.put("from", "");
        ph.put("to", "");
        String formatted = applyPlaceholders(template, ph);
        rebroadcast(formatted, "nextstaffchat.notify.join");
    }

    public void sendLeave(String playerName, String uuid, String server) {
        if (!plugin.getConfigManager().isLeaveEnabled()) return;
        String template = plugin.getConfigManager().string("messages.leave-format", "");
        Map<String, String> ph = new HashMap<>();
        ph.put("player", playerName);
        ph.put("displayname", playerName);
        ph.put("server", server == null ? "" : server);
        ph.put("rank", resolveRank(uuid));
        ph.put("message", "");
        ph.put("gamemode", "");
        ph.put("from", "");
        ph.put("to", "");
        String formatted = applyPlaceholders(template, ph);
        rebroadcast(formatted, "nextstaffchat.notify.leave");
    }

    public void rebroadcast(String formatted, String permission) {
        Component comp = VelocityColorUtil.parse(formatted);

        // 1) Proxy console always sees it.
        plugin.getProxy().getConsoleCommandSource().sendMessage(comp);

        // 2) Deliver DIRECTLY to every online player on the proxy that has the
        //    required permission. This is what makes the plugin work standalone
        //    (no Paper plugin required on backend servers).
        for (Player p : plugin.getProxy().getAllPlayers()) {
            if (permission != null && !permission.isEmpty() && !p.hasPermission(permission)) continue;
            UUID id = p.getUniqueId();
            if (plugin.getMuteManager().isMuted(id)) continue;
            p.sendMessage(comp);
        }

        // 3) Also forward a BROADCAST envelope to backend servers. This is a no-op
        //    on backends without the Paper companion plugin (channel not registered),
        //    but stays for backwards compatibility when both sides are installed.
        if (plugin.getConfigManager().isForwardToBackendsEnabled()) {
            byte[] payload;
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(bout)) {
                out.writeUTF(ChannelConstants.SUB_BROADCAST);
                out.writeUTF(permission == null ? "" : permission);
                out.writeUTF(formatted);
                payload = bout.toByteArray();
            } catch (IOException e) {
                plugin.getLogger().warn("Failed to encode broadcast: {}", e.getMessage());
                payload = null;
            }
            if (payload != null) {
                for (RegisteredServer rs : plugin.getProxy().getAllServers()) {
                    try {
                        rs.sendPluginMessage(NextStaffChatVelocity.CHANNEL, payload);
                    } catch (Exception ignored) {
                        // Server may be empty or not connected; ignore.
                    }
                }
            }
        }

        plugin.getLogManager().log(VelocityColorUtil.strip(formatted));
    }

    private String resolveRank(String uuid) {
        // Best-effort: use the proxy player's "default group" via permissions; LuckPerms
        // Velocity API exposes groups but is not a hard dep here. Returns "default" if
        // not resolvable.
        return "default";
    }

    private String applyPlaceholders(String template, Map<String, String> ph) {
        String out = template == null ? "" : template;
        for (Map.Entry<String, String> e : ph.entrySet()) {
            out = out.replace("%" + e.getKey() + "%", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
