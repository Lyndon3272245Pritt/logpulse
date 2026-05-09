package com.logpulse.drain;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * LogDrainer collects log entries into an internal queue and drains them
 * in configurable batches to a downstream consumer. Supports both
 * time-based and size-based drain triggers.
 */
public class LogDrainer {

    private final DrainConfig config;
    private final BlockingQueue<LogEntry> queue;
    private final Consumer<List<LogEntry>> downstream;
    private volatile boolean running;
    private Thread drainThread;

    public LogDrainer(DrainConfig config, Consumer<List<LogEntry>> downstream) {
        if (config == null) throw new IllegalArgumentException("DrainConfig must not be null");
        if (downstream == null) throw new IllegalArgumentException("Downstream consumer must not be null");
        this.config = config;
        this.downstream = downstream;
        this.queue = new LinkedBlockingQueue<>(config.getMaxQueueSize());
    }

    public boolean offer(LogEntry entry) {
        if (entry == null) return false;
        boolean accepted = queue.offer(entry);
        if (accepted && queue.size() >= config.getBatchSize()) {
            triggerDrain();
        }
        return accepted;
    }

    public void start() {
        running = true;
        drainThread = new Thread(this::drainLoop, "logpulse-drainer");
        drainThread.setDaemon(true);
        drainThread.start();
    }

    public void stop() {
        running = false;
        if (drainThread != null) {
            drainThread.interrupt();
        }
        flush();
    }

    public void flush() {
        List<LogEntry> batch = new ArrayList<>();
        queue.drainTo(batch);
        if (!batch.isEmpty()) {
            downstream.accept(Collections.unmodifiableList(batch));
        }
    }

    private void drainLoop() {
        while (running) {
            try {
                TimeUnit.MILLISECONDS.sleep(config.getDrainIntervalMs());
                flush();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void triggerDrain() {
        if (drainThread != null && drainThread.isAlive()) {
            drainThread.interrupt();
        }
    }

    public int queueSize() {
        return queue.size();
    }

    public boolean isRunning() {
        return running;
    }
}
