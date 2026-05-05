package com.logpulse.dedup;

import com.logpulse.model.LogEntry;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Deduplicates log entries within a configurable time window.
 * Entries with identical service, level, and message are suppressed
 * if a matching entry was seen within the dedup window.
 */
public class LogDeduplicator {

    private final long windowMillis;
    private final int maxCacheSize;

    // Key: fingerprint -> last-seen timestamp
    private final LinkedHashMap<String, Instant> seen;

    public LogDeduplicator(long windowMillis, int maxCacheSize) {
        if (windowMillis < 0) throw new IllegalArgumentException("windowMillis must be >= 0");
        if (maxCacheSize <= 0) throw new IllegalArgumentException("maxCacheSize must be > 0");
        this.windowMillis = windowMillis;
        this.maxCacheSize = maxCacheSize;
        this.seen = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
                return size() > maxCacheSize;
            }
        };
    }

    /**
     * Returns {@code true} if the entry is a duplicate and should be suppressed.
     */
    public synchronized boolean isDuplicate(LogEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        String key = fingerprint(entry);
        Instant now = entry.getTimestamp() != null ? entry.getTimestamp() : Instant.now();
        Instant last = seen.get(key);
        if (last != null && now.toEpochMilli() - last.toEpochMilli() < windowMillis) {
            return true;
        }
        seen.put(key, now);
        return false;
    }

    /** Clears the internal dedup cache. */
    public synchronized void reset() {
        seen.clear();
    }

    public int getCacheSize() {
        return seen.size();
    }

    private String fingerprint(LogEntry entry) {
        return entry.getService() + "\0" + entry.getLevel() + "\0" + entry.getMessage();
    }
}
