package com.vehector.nextstaffchat.velocity.commands;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import com.vehector.nextstaffchat.velocity.utils.VelocityColorUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public final class StaffChatVelocityCommand implements SimpleCommand {

    private final NextStaffChatVelocity plugin;

    public StaffChatVelocityCommand(NextStaffChatVelocity plugin) { this.plugin = plugin; }

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player p)) {
            // Console-side: send a chat message as system.
            if (args.length == 0) {
                src.sendMessage(VelocityColorUtil.parse(
                        plugin.getConfigManager().string("messages.usage", "Usage: /sc <message>")));
                return;
            }
            String msg = String.join(" ", args);
            plugin.getChatService().handleIncoming(ChannelConstants.SUB_CHAT,
                    "Console", "00000000-0000-0000-0000-000000000000",
                    "proxy", msg, "", null);
            return;
        }

        if (!p.hasPermission("nextstaffchat.use")) {
            p.sendMessage(VelocityColorUtil.parse(
                    plugin.getConfigManager().string("messages.no-permission",
                            "<red>You don't have permission.")));
            return;
        }

        if (args.length == 0) {
            if (!plugin.getConfigManager().isToggleEnabled()) {
                p.sendMessage(VelocityColorUtil.parse(
                        plugin.getConfigManager().string("messages.toggle-disabled",
                                "<red>Staff chat toggling is disabled.")));
                return;
            }
            if (!p.hasPermission("nextstaffchat.toggle")) {
                p.sendMessage(VelocityColorUtil.parse(
                        plugin.getConfigManager().string("messages.no-permission",
                                "<red>You don't have permission.")));
                return;
            }
            boolean enabled = plugin.getToggleManager().toggle(p.getUniqueId());
            p.sendMessage(VelocityColorUtil.parse(
                    plugin.getConfigManager().string(
                            enabled ? "messages.toggle-on" : "messages.toggle-off",
                            enabled ? "<green>Staff chat enabled." : "<red>Staff chat disabled.")));
            // Note: toggle on Velocity is informational; Paper handles the actual chat
            // interception. Both sides keep state in sync via /sc.
            return;
        }

        String msg = String.join(" ", args);
        String server = p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("proxy");
        plugin.getChatService().handleIncoming(ChannelConstants.SUB_CHAT,
                p.getUsername(), p.getUniqueId().toString(), server, msg, "", null);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("nextstaffchat.use");
    }
}
