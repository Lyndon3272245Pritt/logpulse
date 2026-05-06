package com.logpulse.snapshot;

import com.logpulse.model.LogEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages periodic snapshots of buffered log entries to disk,
 * rotating old snapshots when the configured maximum is exceeded.
 */
public class LogSnapshotManager {

    private static final Logger LOGGER = Logger.getLogger(LogSnapshotManager.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneOffset.UTC);

    private final SnapshotConfig config;
    private final Deque<Path> snapshotHistory = new ArrayDeque<>();

    public LogSnapshotManager(SnapshotConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
    }

    /**
     * Writes the provided log entries to a new snapshot file.
     * Rotates old snapshots if the history exceeds the configured maximum.
     *
     * @param entries the log entries to snapshot
     * @return the path of the written snapshot file
     * @throws IOException if writing fails
     */
    public synchronized Path takeSnapshot(List<LogEntry> entries) throws IOException {
        Files.createDirectories(config.getOutputDirectory());

        String filename = "snapshot_" + TIMESTAMP_FMT.format(Instant.now()) + ".log";
        Path snapshotPath = config.getOutputDirectory().resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(snapshotPath)) {
            for (LogEntry entry : entries) {
                writer.write(entry.getRaw());
                writer.newLine();
            }
        }

        snapshotHistory.addLast(snapshotPath);
        LOGGER.info("Snapshot written: " + snapshotPath + " (" + entries.size() + " entries)");

        pruneOldSnapshots();
        return snapshotPath;
    }

    /**
     * Returns an unmodifiable view of the current snapshot history (oldest first).
     */
    public synchronized List<Path> getSnapshotHistory() {
        return List.copyOf(snapshotHistory);
    }

    private void pruneOldSnapshots() {
        while (snapshotHistory.size() > config.getMaxSnapshots()) {
            Path oldest = snapshotHistory.removeFirst();
            try {
                Files.deleteIfExists(oldest);
                LOGGER.info("Pruned old snapshot: " + oldest);
            } catch (IOException e) {
                LOGGER.warning("Failed to delete old snapshot: " + oldest + " — " + e.getMessage());
            }
        }
    }
}
