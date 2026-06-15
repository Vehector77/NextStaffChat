package com.vehector.nextstaffchat.paper.managers;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import com.vehector.nextstaffchat.paper.utils.ColorUtil;
import com.vehector.nextstaffchat.paper.utils.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Core staff-chat broadcaster. Decides whether to dispatch to the proxy
 * or to broadcast locally, formats messages, plays sounds and writes logs.
 */
public final class StaffChatService {

    private final NextStaffChatPaper plugin;

    public StaffChatService(NextStaffChatPaper plugin) {
        this.plugin = plugin;
    }

    public void sendChat(Player player, String message) {
        if (!plugin.getConfigManager().isChatEnabled()) return;
        if (plugin.getConfigManager().isProxyMode()) {
            sendToProxy(ChannelConstants.SUB_CHAT, player, Map.of("message", message));
        } else {
            broadcastLocal(plugin.getConfigManager().msg("chat-format"), player, Map.of(
                    "message", message,
                    "server", plugin.getConfigManager().getServerName()));
        }
    }

    public void sendJoin(Player player) {
        if (!plugin.getConfigManager().isJoinEnabled()) return;
        if (plugin.getConfigManager().isProxyMode()) {
            sendToProxy(ChannelConstants.SUB_JOIN, player, Map.of());
        } else {
            broadcastLocal(plugin.getConfigManager().msg("join-format"), player, Map.of());
        }
    }

    public void sendLeave(Player player) {
        if (!plugin.getConfigManager().isLeaveEnabled()) return;
        if (plugin.getConfigManager().isProxyMode()) {
            sendToProxy(ChannelConstants.SUB_LEAVE, player, Map.of());
        } else {
            broadcastLocal(plugin.getConfigManager().msg("leave-format"), player, Map.of());
        }
    }

    public void sendGamemode(Player player, String gamemode) {
        if (!plugin.getConfigManager().isGamemodeEnabled()) return;
        Map<String, String> extra = Map.of("gamemode", gamemode);
        if (plugin.getConfigManager().isProxyMode()) {
            sendToProxy(ChannelConstants.SUB_GAMEMODE, player, extra);
        } else {
            broadcastLocal(plugin.getConfigManager().msg("gamemode-format"), player, extra);
        }
    }

    /** Called by the PluginMessageListener when Velocity rebroadcasts a fully formatted line. */
    public void receiveBroadcast(String formatted, String notifyPermission) {
        Component comp = ColorUtil.parse(formatted);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(notifyPermission)) continue;
            if (plugin.getMuteManager().isMuted(p.getUniqueId())) continue;
            p.sendMessage(comp);
        }
        // Console always receives.
        Bukkit.getConsoleSender().sendMessage(comp);
        plugin.getLogManager().log(ColorUtil.strip(formatted));
        playSoundForReceivers(notifyPermission);
    }

    private void broadcastLocal(String template, Player player, Map<String, String> extras) {
        Map<String, String> map = new HashMap<>(extras);
        String formatted = PlaceholderUtil.apply(plugin, player, template, map);
        Component comp = ColorUtil.parse(formatted);
        String perm = "nextstaffchat.use";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(perm)) continue;
            if (plugin.getMuteManager().isMuted(p.getUniqueId())) continue;
            p.sendMessage(comp);
        }
        Bukkit.getConsoleSender().sendMessage(comp);
        plugin.getLogManager().log(ColorUtil.strip(formatted));
        playSoundForReceivers(perm);
    }

    private void playSoundForReceivers(String perm) {
        if (!plugin.getConfigManager().isSoundEnabled() || plugin.getConfigManager().getSound() == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(perm)) continue;
            if (plugin.getMuteManager().isMuted(p.getUniqueId())) continue;
            p.playSound(p.getLocation(), plugin.getConfigManager().getSound(),
                    plugin.getConfigManager().getSoundVolume(),
                    plugin.getConfigManager().getSoundPitch());
        }
    }

    private void sendToProxy(String sub, Player player, Map<String, String> extra) {
        Player any = player != null ? player : Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (any == null) {
            // Nobody online: cannot forward through plugin messaging. Fallback to local.
            String tpl = templateFor(sub);
            broadcastLocal(tpl, player, extra);
            return;
        }
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bout)) {
            out.writeUTF(sub);
            out.writeUTF(player == null ? "console" : player.getName());
            out.writeUTF(player == null ? "00000000-0000-0000-0000-000000000000"
                                        : player.getUniqueId().toString());
            out.writeUTF(plugin.getConfigManager().getServerName());
            out.writeUTF(extra.getOrDefault("message", ""));
            out.writeUTF(extra.getOrDefault("gamemode", ""));
            any.sendPluginMessage(plugin, ChannelConstants.CHANNEL, bout.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Could not forward message to proxy: " + e.getMessage());
        }
    }

    private String templateFor(String sub) {
        return switch (sub) {
            case ChannelConstants.SUB_CHAT -> plugin.getConfigManager().msg("chat-format");
            case ChannelConstants.SUB_JOIN -> plugin.getConfigManager().msg("join-format");
            case ChannelConstants.SUB_LEAVE -> plugin.getConfigManager().msg("leave-format");
            case ChannelConstants.SUB_SWITCH -> plugin.getConfigManager().msg("switch-format");
            case ChannelConstants.SUB_GAMEMODE -> plugin.getConfigManager().msg("gamemode-format");
            default -> "";
        };
    }
}
