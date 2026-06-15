package com.vehector.nextstaffchat.velocity.commands;

import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import com.vehector.nextstaffchat.velocity.utils.VelocityColorUtil;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public final class NextStaffChatVelocityCommand implements SimpleCommand {

    private final NextStaffChatVelocity plugin;

    public NextStaffChatVelocityCommand(NextStaffChatVelocity plugin) { this.plugin = plugin; }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(VelocityColorUtil.parse(
                    "<gray>NextStaffChat (Velocity) by Vehector. Use /nsc reload | /nsc mute"));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!invocation.source().hasPermission("nextstaffchat.admin")) {
                    invocation.source().sendMessage(VelocityColorUtil.parse(
                            plugin.getConfigManager().string("messages.no-permission",
                                    "<red>No permission.")));
                    return;
                }
                plugin.reload();
                invocation.source().sendMessage(VelocityColorUtil.parse(
                        plugin.getConfigManager().string("messages.reload-success",
                                "<green>Reloaded.")));
            }
            case "mute" -> {
                if (!(invocation.source() instanceof Player p)) {
                    invocation.source().sendMessage(VelocityColorUtil.parse("<red>Players only."));
                    return;
                }
                if (!p.hasPermission("nextstaffchat.mute")) {
                    p.sendMessage(VelocityColorUtil.parse(plugin.getConfigManager().string(
                            "messages.no-permission", "<red>No permission.")));
                    return;
                }
                boolean muted = plugin.getMuteManager().toggle(p.getUniqueId());
                p.sendMessage(VelocityColorUtil.parse(plugin.getConfigManager().string(
                        muted ? "messages.mute-on" : "messages.mute-off",
                        muted ? "<gray>Muted." : "<gray>Unmuted.")));
            }
            default -> invocation.source().sendMessage(VelocityColorUtil.parse(
                    plugin.getConfigManager().string("messages.usage", "<gray>Usage: /sc [msg]")));
        }
    }
}
