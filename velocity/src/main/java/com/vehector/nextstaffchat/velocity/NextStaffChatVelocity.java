package com.vehector.nextstaffchat.velocity;

import com.google.inject.Inject;
import com.vehector.nextstaffchat.common.ChannelConstants;
import com.vehector.nextstaffchat.velocity.commands.NextStaffChatVelocityCommand;
import com.vehector.nextstaffchat.velocity.commands.StaffChatVelocityCommand;
import com.vehector.nextstaffchat.velocity.listeners.ConnectionListener;
import com.vehector.nextstaffchat.velocity.listeners.VelocityChatListener;
import com.vehector.nextstaffchat.velocity.listeners.VelocityPluginMessageListener;
import com.vehector.nextstaffchat.velocity.managers.VelocityConfigManager;
import com.vehector.nextstaffchat.velocity.managers.VelocityLogManager;
import com.vehector.nextstaffchat.velocity.managers.VelocityMuteManager;
import com.vehector.nextstaffchat.velocity.managers.VelocityStaffChatService;
import com.vehector.nextstaffchat.velocity.managers.VelocityToggleManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "nextstaffchat",
        name = "NextStaffChat",
        version = "1.0.0",
        description = "Fully customizable cross-server staff chat",
        authors = {"Vehector"}
)
public final class NextStaffChatVelocity {

    public static final MinecraftChannelIdentifier CHANNEL =
            MinecraftChannelIdentifier.create(ChannelConstants.CHANNEL_NAMESPACE,
                    ChannelConstants.CHANNEL_NAME);

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDir;

    private VelocityConfigManager configManager;
    private VelocityToggleManager toggleManager;
    private VelocityMuteManager muteManager;
    private VelocityLogManager logManager;
    private VelocityStaffChatService chatService;

    @Inject
    public NextStaffChatVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDir) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDir = dataDir;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        this.configManager = new VelocityConfigManager(this, dataDir);
        this.toggleManager = new VelocityToggleManager();
        this.muteManager = new VelocityMuteManager();
        this.logManager = new VelocityLogManager(this, dataDir);
        this.chatService = new VelocityStaffChatService(this);

        proxy.getChannelRegistrar().register(CHANNEL);

        proxy.getEventManager().register(this, new VelocityPluginMessageListener(this));
        proxy.getEventManager().register(this, new ConnectionListener(this));
        proxy.getEventManager().register(this, new VelocityChatListener(this));

        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("staffchat").aliases("sc", "schat").plugin(this).build(),
                new StaffChatVelocityCommand(this));
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("nextstaffchat").aliases("nsc").plugin(this).build(),
                new NextStaffChatVelocityCommand(this));

        logger.info("NextStaffChat (Velocity) enabled.");
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        if (logManager != null) logManager.close();
    }

    public void reload() {
        configManager.reload();
        logManager.reload();
    }

    public ProxyServer getProxy() { return proxy; }
    public Logger getLogger() { return logger; }
    public VelocityConfigManager getConfigManager() { return configManager; }
    public VelocityToggleManager getToggleManager() { return toggleManager; }
    public VelocityMuteManager getMuteManager() { return muteManager; }
    public VelocityLogManager getLogManager() { return logManager; }
    public VelocityStaffChatService getChatService() { return chatService; }
}
