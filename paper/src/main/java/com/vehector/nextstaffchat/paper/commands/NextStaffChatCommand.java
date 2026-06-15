package com.vehector.nextstaffchat.paper.commands;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import com.vehector.nextstaffchat.paper.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class NextStaffChatCommand implements CommandExecutor {

    private final NextStaffChatPaper plugin;

    public NextStaffChatCommand(NextStaffChatPaper plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.parse("<gray>NextStaffChat v" + plugin.getDescription().getVersion()
                    + " by Vehector. Use /nsc reload | /nsc mute"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("nextstaffchat.admin")) {
                    sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("no-permission")));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("reload-success")));
            }
            case "mute" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(ColorUtil.parse("<red>Players only."));
                    return true;
                }
                if (!p.hasPermission("nextstaffchat.mute")) {
                    p.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("no-permission")));
                    return true;
                }
                boolean muted = plugin.getMuteManager().toggle(p.getUniqueId());
                p.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg(
                        muted ? "mute-on" : "mute-off")));
            }
            default -> sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().msg("usage")));
        }
        return true;
    }
}
