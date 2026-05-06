package com.logpulse.checkpoint;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages read checkpoints for log files so that LogPulse can resume
 * tailing from the last known position after a restart.
 */
public class CheckpointManager {

    private static final Logger log = Logger.getLogger(CheckpointManager.class.getName());

    private final Path checkpointFile;
    private final Map<String, Long> offsets = new ConcurrentHashMap<>();

    public CheckpointManager(CheckpointConfig config) throws IOException {
        this.checkpointFile = Paths.get(config.getCheckpointFilePath());
        if (Files.exists(checkpointFile)) {
            load();
        }
    }

    /** Returns the last saved byte offset for the given source path, or 0 if unknown. */
    public long getOffset(String sourcePath) {
        return offsets.getOrDefault(sourcePath, 0L);
    }

    /** Records the current byte offset for the given source path and persists immediately. */
    public synchronized void saveOffset(String sourcePath, long offset) throws IOException {
        offsets.put(sourcePath, offset);
        persist();
    }

    /** Removes the checkpoint entry for the given source path. */
    public synchronized void clearOffset(String sourcePath) throws IOException {
        offsets.remove(sourcePath);
        persist();
    }

    /** Returns an unmodifiable snapshot of all tracked offsets. */
    public Map<String, Long> getAllOffsets() {
        return Map.copyOf(offsets);
    }

    // -------------------------------------------------------------------------

    private void load() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(checkpointFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int sep = line.lastIndexOf('=');
                if (sep < 1) continue;
                String path = line.substring(0, sep).trim();
                long offset = Long.parseLong(line.substring(sep + 1).trim());
                offsets.put(path, offset);
            }
        }
        log.info("Loaded " + offsets.size() + " checkpoint(s) from " + checkpointFile);
    }

    private void persist() throws IOException {
        Path tmp = checkpointFile.resolveSibling(checkpointFile.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmp)) {
            writer.write("# LogPulse checkpoint file — do not edit manually");
            writer.newLine();
            for (Map.Entry<String, Long> entry : offsets.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        }
        Files.move(tmp, checkpointFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
