package com.vehector.nextstaffchat.velocity.managers;

import com.vehector.nextstaffchat.velocity.NextStaffChatVelocity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class VelocityLogManager {

    private final NextStaffChatVelocity plugin;
    private final Path dataDir;
    private volatile boolean enabled;
    private SimpleDateFormat dateFormat;
    private Path file;
    private LinkedBlockingQueue<String> queue;
    private Thread worker;
    private volatile boolean running;

    public VelocityLogManager(NextStaffChatVelocity plugin, Path dataDir) {
        this.plugin = plugin;
        this.dataDir = dataDir;
        reload();
    }

    public synchronized void reload() {
        close();
        this.enabled = plugin.getConfigManager().bool("logging.enabled", false);
        if (!enabled) return;
        this.dateFormat = new SimpleDateFormat(
                plugin.getConfigManager().string("logging.date-format", "yyyy-MM-dd HH:mm:ss"));
        this.file = dataDir.resolve(plugin.getConfigManager().string("logging.file", "staffchat.log"));
        this.queue = new LinkedBlockingQueue<>();
        this.running = true;
        this.worker = new Thread(this::loop, "NextStaffChat-Velocity-Logger");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public void log(String line) {
        if (!enabled || queue == null) return;
        queue.offer("[" + dateFormat.format(new Date()) + "] " + line);
    }

    private void loop() {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            while (running) {
                String line = queue.poll(1, TimeUnit.SECONDS);
                if (line != null) { w.write(line); w.newLine(); w.flush(); }
            }
        } catch (IOException | InterruptedException e) {
            plugin.getLogger().warn("Log writer stopped: {}", e.getMessage());
        }
    }

    public synchronized void close() {
        running = false;
        if (worker != null) worker.interrupt();
        worker = null;
        queue = null;
    }
}
