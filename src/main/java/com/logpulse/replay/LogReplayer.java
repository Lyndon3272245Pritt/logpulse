package com.logpulse.replay;

import com.logpulse.model.LogEntry;
import com.logpulse.parser.LogEntryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Replays log entries from a file, respecting time windows and speed multipliers.
 */
public class LogReplayer {

    private static final Logger log = Logger.getLogger(LogReplayer.class.getName());

    private final LogReplayConfig config;
    private final LogEntryParser parser;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public LogReplayer(LogReplayConfig config, LogEntryParser parser) {
        this.config = config;
        this.parser = parser;
    }

    public void start(Consumer<LogEntry> listener) {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("LogReplayer is already running");
        }
        Thread thread = new Thread(() -> replayLoop(listener), "log-replayer");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    private void replayLoop(Consumer<LogEntry> listener) {
        do {
            try (BufferedReader reader = Files.newBufferedReader(Path.of(config.getSourcePath()))) {
                String line;
                Instant previousTimestamp = null;
                long previousWallTime = System.currentTimeMillis();

                while (running.get() && (line = reader.readLine()) != null) {
                    LogEntry entry = parser.parse(line);
                    if (entry == null) continue;

                    Instant ts = entry.getTimestamp();
                    if (ts.isBefore(config.getFromTime()) || ts.isAfter(config.getToTime())) continue;

                    if (previousTimestamp != null) {
                        long logGapMs = ts.toEpochMilli() - previousTimestamp.toEpochMilli();
                        long sleepMs = (long) (logGapMs / config.getSpeedMultiplier());
                        long elapsed = System.currentTimeMillis() - previousWallTime;
                        long remaining = sleepMs - elapsed;
                        if (remaining > 0) Thread.sleep(remaining);
                    }

                    previousTimestamp = ts;
                    previousWallTime = System.currentTimeMillis();
                    listener.accept(entry);
                }
            } catch (IOException e) {
                log.severe("Error reading replay source: " + e.getMessage());
                running.set(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running.set(false);
            }
        } while (config.isLoop() && running.get());

        running.set(false);
    }
}
