package com.vehector.nextstaffchat.paper.managers;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ConfigManager {

    private final NextStaffChatPaper plugin;
    private FileConfiguration messages;
    private File messagesFile;

    // cached values
    private boolean proxyMode;
    private String serverName;
    private boolean toggleEnabled;
    private boolean togglePersist;
    private boolean resetOnSwitch;
    private boolean modChat, modJoin, modLeave, modSwitch, modGamemode;
    private boolean soundEnabled;
    private Sound sound;
    private float soundVolume, soundPitch;
    private boolean papiEnabled;

    public ConfigManager(NextStaffChatPaper plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();
        this.proxyMode = c.getBoolean("proxy-mode", true);
        this.serverName = c.getString("server-name", "server");
        this.toggleEnabled = c.getBoolean("toggle.enabled", true);
        this.togglePersist = c.getBoolean("toggle.persist-across-sessions", false);
        this.resetOnSwitch = c.getBoolean("toggle.reset-on-server-switch", true);
        this.modChat = c.getBoolean("modules.chat", true);
        this.modJoin = c.getBoolean("modules.join-notify", true);
        this.modLeave = c.getBoolean("modules.leave-notify", true);
        this.modSwitch = c.getBoolean("modules.server-switch-notify", true);
        this.modGamemode = c.getBoolean("modules.gamemode-change-notify", true);
        this.soundEnabled = c.getBoolean("sound.enabled", true);
        String soundName = c.getString("sound.type", "BLOCK_NOTE_BLOCK_PLING");
        Sound parsed = null;
        try {
            if (soundName != null && !soundName.isEmpty()) {
                parsed = Sound.valueOf(soundName.toUpperCase());
            }
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().warning("Unknown sound: " + soundName);
        }
        this.sound = parsed;
        this.soundVolume = (float) c.getDouble("sound.volume", 0.6);
        this.soundPitch = (float) c.getDouble("sound.pitch", 1.3);
        this.papiEnabled = c.getBoolean("placeholderapi", true);

        // messages.yml
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
        try (InputStreamReader r = new InputStreamReader(
                plugin.getResource("messages.yml"), StandardCharsets.UTF_8)) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(r));
        } catch (IOException | NullPointerException ignored) {}
    }

    public String msg(String key) { return messages.getString(key, ""); }

    public boolean isProxyMode() { return proxyMode; }
    public String getServerName() { return serverName; }
    public boolean isToggleEnabled() { return toggleEnabled; }
    public boolean isTogglePersist() { return togglePersist; }
    public boolean isResetOnSwitch() { return resetOnSwitch; }
    public boolean isChatEnabled() { return modChat; }
    public boolean isJoinEnabled() { return modJoin; }
    public boolean isLeaveEnabled() { return modLeave; }
    public boolean isSwitchEnabled() { return modSwitch; }
    public boolean isGamemodeEnabled() { return modGamemode; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public Sound getSound() { return sound; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }
    public boolean isPapiEnabled() { return papiEnabled; }
}
