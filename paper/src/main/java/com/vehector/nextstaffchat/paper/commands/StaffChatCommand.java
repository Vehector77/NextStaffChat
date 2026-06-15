package com.vehector.nextstaffchat.paper.commands;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import com.vehector.nextstaffchat.paper.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class StaffChatCommand implements CommandExecutor {

    private final NextStaffChatPaper plugin;

    public StaffChatCommand(NextStaffChatPaper plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            // Console: must include a message.
            if (args.length == 0) {
                sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("usage")));
                return true;
            }
            String msg = String.join(" ", args);
            plugin.getChatService().sendChat(null, msg);
            return true;
        }

        if (!p.hasPermission("nextstaffchat.use")) {
            p.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("no-permission")));
            return true;
        }

        if (args.length == 0) {
            if (!plugin.getConfigManager().isToggleEnabled()) {
                p.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("toggle-disabled")));
                return true;
            }
            if (!p.hasPermission("nextstaffchat.toggle")) {
                p.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("no-permission")));
                return true;
            }
            boolean enabled = plugin.getToggleManager().toggle(p.getUniqueId());
            p.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg(
                    enabled ? "toggle-on" : "toggle-off")));
            return true;
        }

        String msg = String.join(" ", args);
        plugin.getChatService().sendChat(p, msg);
        return true;
    }
}
