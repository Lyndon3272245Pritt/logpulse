package com.logpulse.cursor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-source byte offsets (cursors) so that log tailing can resume
 * from the last known position after a restart or interruption.
 */
public class LogCursorTracker {

    private final CursorConfig config;
    private final ConcurrentHashMap<String, Long> cursors;

    public LogCursorTracker(CursorConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
        this.cursors = new ConcurrentHashMap<>();
    }

    /**
     * Records the current read offset for the given source identifier.
     *
     * @param sourceId unique identifier for the log source (e.g. file path or service name)
     * @param offset   byte offset representing how far the source has been read
     */
    public void updateCursor(String sourceId, long offset) {
        if (sourceId == null || sourceId.isBlank()) throw new IllegalArgumentException("sourceId must not be blank");
        if (offset < 0) throw new IllegalArgumentException("offset must be >= 0");
        if (!cursors.containsKey(sourceId) && cursors.size() >= config.getMaxTrackedSources()) {
            throw new IllegalStateException("Maximum tracked sources limit reached: " + config.getMaxTrackedSources());
        }
        cursors.put(sourceId, offset);
    }

    /**
     * Returns the last known cursor offset for a source, if any.
     */
    public Optional<Long> getCursor(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) return Optional.empty();
        return Optional.ofNullable(cursors.get(sourceId));
    }

    /**
     * Removes the cursor entry for the given source.
     */
    public void removeCursor(String sourceId) {
        cursors.remove(sourceId);
    }

    /**
     * Returns an unmodifiable snapshot of all current cursors.
     */
    public Map<String, Long> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(cursors));
    }

    /**
     * Resets all tracked cursors.
     */
    public void reset() {
        cursors.clear();
    }

    public int trackedSourceCount() {
        return cursors.size();
    }

    public CursorConfig getConfig() {
        return config;
    }
}
