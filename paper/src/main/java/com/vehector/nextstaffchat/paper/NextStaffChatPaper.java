package com.vehector.nextstaffchat.paper;

import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.paper.commands.NextStaffChatCommand;
import com.vehector.nextstaffchat.paper.commands.StaffChatCommand;
import com.vehector.nextstaffchat.paper.listeners.ChatListener;
import com.vehector.nextstaffchat.paper.listeners.GameModeListener;
import com.vehector.nextstaffchat.paper.listeners.JoinQuitListener;
import com.vehector.nextstaffchat.paper.listeners.PluginMessageListener;
import com.vehector.nextstaffchat.paper.managers.ConfigManager;
import com.vehector.nextstaffchat.paper.managers.LogManager;
import com.vehector.nextstaffchat.paper.managers.MuteManager;
import com.vehector.nextstaffchat.paper.managers.StaffChatService;
import com.vehector.nextstaffchat.paper.managers.ToggleManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NextStaffChatPaper extends JavaPlugin {

    private ConfigManager configManager;
    private ToggleManager toggleManager;
    private MuteManager muteManager;
    private LogManager logManager;
    private StaffChatService chatService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.configManager = new ConfigManager(this);
        this.toggleManager = new ToggleManager();
        this.muteManager = new MuteManager();
        this.logManager = new LogManager(this);
        this.chatService = new StaffChatService(this);

        // Register Plugin Messaging channel both ways (needed for IN + OUT).
        getServer().getMessenger().registerOutgoingPluginChannel(this, ChannelConstants.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, ChannelConstants.CHANNEL,
                new PluginMessageListener(this));

        // Commands
        getCommand("staffchat").setExecutor(new StaffChatCommand(this));
        getCommand("nextstaffchat").setExecutor(new NextStaffChatCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GameModeListener(this), this);

        getLogger().info("NextStaffChat enabled. Proxy mode: " + configManager.isProxyMode());
    }

    @Override
    public void onDisable() {
        if (logManager != null) logManager.close();
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        logManager.reload();
    }

    public ConfigManager getConfigManager() { return configManager; }
    public ToggleManager getToggleManager() { return toggleManager; }
    public MuteManager getMuteManager() { return muteManager; }
    public LogManager getLogManager() { return logManager; }
    public StaffChatService getChatService() { return chatService; }
}
