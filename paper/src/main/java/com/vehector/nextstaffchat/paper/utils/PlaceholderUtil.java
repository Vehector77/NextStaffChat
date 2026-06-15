package com.vehector.nextstaffchat.paper.utils;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the built-in placeholder set, and (when PlaceholderAPI is installed)
 * forwards %papi_xxx% style placeholders through PlaceholderAPI.
 */
public final class PlaceholderUtil {

    private static Boolean papiPresent;

    private PlaceholderUtil() {}

    public static String apply(NextStaffChatPaper plugin, OfflinePlayer player, String template,
                               Map<String, String> extra) {
        if (template == null) return "";
        String out = template;
        Map<String, String> map = new HashMap<>();
        String name = player == null ? "console" : (player.getName() == null ? "unknown" : player.getName());
        map.put("player", name);
        map.put("displayname",
                (player instanceof Player p) ? ColorUtil.strip(stripDisplayName(p)) : name);
        map.put("server", plugin.getConfigManager().getServerName());
        map.put("rank", resolveRank(plugin, player));
        if (extra != null) map.putAll(extra);

        for (Map.Entry<String, String> e : map.entrySet()) {
            out = out.replace("%" + e.getKey() + "%", e.getValue() == null ? "" : e.getValue());
        }

        if (plugin.getConfigManager().isPapiEnabled() && isPapiPresent() && player != null) {
            out = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, out);
        }
        return out;
    }

    private static String stripDisplayName(Player p) {
        try { return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(p.displayName()); }
        catch (Throwable t) { return p.getName(); }
    }

    private static String resolveRank(NextStaffChatPaper plugin, OfflinePlayer player) {
        if (player == null) return "";
        if (plugin.getConfigManager().isPapiEnabled() && isPapiPresent()) {
            try {
                String r = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%vault_rank%");
                if (r != null && !r.isEmpty() && !r.equals("%vault_rank%")) return r;
                String g = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%luckperms_primary_group_name%");
                if (g != null && !g.isEmpty() && !g.equals("%luckperms_primary_group_name%")) return g;
            } catch (Throwable ignored) {}
        }
        return "default";
    }

    private static boolean isPapiPresent() {
        if (papiPresent == null) {
            papiPresent = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        }
        return papiPresent;
    }
}
