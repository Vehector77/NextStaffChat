package com.vehector.nextstaffchat.velocity.managers;

import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VelocityConfigManager {

    private final NextStaffChatVelocity plugin;
    private final Path dataDir;
    private YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    public VelocityConfigManager(NextStaffChatVelocity plugin, Path dataDir) {
        this.plugin = plugin;
        this.dataDir = dataDir;
        reload();
    }

    public void reload() {
        try {
            Files.createDirectories(dataDir);
            Path file = dataDir.resolve("config.yml");
            if (!Files.exists(file)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) Files.copy(in, file);
                }
            }
            this.loader = YamlConfigurationLoader.builder()
                    .nodeStyle(NodeStyle.BLOCK).indent(2).path(file).build();
            this.root = loader.load();
        } catch (IOException e) {
            plugin.getLogger().error("Failed to load Velocity config", e);
        }
    }

    public String msg(String key) {
        ConfigurationNode n = root.node((Object[]) key.split("\\."));
        return n.getString("");
    }

    public String string(String path, String def) {
        ConfigurationNode n = root.node((Object[]) path.split("\\."));
        return n.getString(def);
    }

    public boolean bool(String path, boolean def) {
        ConfigurationNode n = root.node((Object[]) path.split("\\."));
        return n.getBoolean(def);
    }

    public boolean isToggleEnabled() { return bool("toggle.enabled", true); }
    public boolean isChatEnabled() { return bool("modules.chat", true); }
    public boolean isJoinEnabled() { return bool("modules.join-notify", true); }
    public boolean isLeaveEnabled() { return bool("modules.leave-notify", true); }
    public boolean isSwitchEnabled() { return bool("modules.server-switch-notify", true); }
    public boolean isGamemodeEnabled() { return bool("modules.gamemode-change-notify", true); }

    /** Whether Velocity itself should intercept chat (toggle mode + prefix). */
    public boolean isChatInterceptEnabled() { return bool("standalone.intercept-chat", true); }
    /** Optional message prefix that routes a normal chat message to staff chat. */
    public String chatPrefix() { return string("standalone.chat-prefix", ""); }
    /** Forward the formatted broadcast to backend servers (only useful if the Paper
     *  companion plugin is installed there). Safe to leave enabled when running
     *  standalone — backends without the channel will simply drop the message. */
    public boolean isForwardToBackendsEnabled() { return bool("standalone.forward-to-backends", true); }
}
