package com.vehector.nextstaffchat.paper.managers;

import com.vehector.nextstaffchat.paper.NextStaffChatPaper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Async log writer. A dedicated daemon thread consumes the queue so the
 * server tick is never blocked by disk IO.
 */
public final class LogManager {

    private final NextStaffChatPaper plugin;
    private volatile boolean enabled;
    private SimpleDateFormat dateFormat;
    private File file;
    private LinkedBlockingQueue<String> queue;
    private Thread worker;
    private volatile boolean running;

    public LogManager(NextStaffChatPaper plugin) {
        this.plugin = plugin;
        reload();
    }

    public synchronized void reload() {
        close();
        this.enabled = plugin.getConfig().getBoolean("logging.enabled", false);
        if (!enabled) return;
        this.dateFormat = new SimpleDateFormat(
                plugin.getConfig().getString("logging.date-format", "yyyy-MM-dd HH:mm:ss"));
        String name = plugin.getConfig().getString("logging.file", "staffchat.log");
        this.file = new File(plugin.getDataFolder(), name);
        this.queue = new LinkedBlockingQueue<>();
        this.running = true;
        this.worker = new Thread(this::loop, "NextStaffChat-Logger");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public void log(String line) {
        if (!enabled || queue == null) return;
        queue.offer("[" + dateFormat.format(new Date()) + "] " + line);
    }

    private void loop() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
            while (running) {
                String line = queue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                if (line != null) {
                    w.write(line);
                    w.newLine();
                    w.flush();
                }
            }
        } catch (IOException | InterruptedException e) {
            plugin.getLogger().warning("Log writer stopped: " + e.getMessage());
        }
    }

    public synchronized void close() {
        running = false;
        if (worker != null) worker.interrupt();
        worker = null;
        queue = null;
    }
}
