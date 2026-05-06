package com.logpulse.throttle;

import com.logpulse.model.LogEntry;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Throttles log entries per service/level combination to prevent output flooding.
 * Uses a sliding window approach to count entries within the configured time window.
 */
public class LogThrottler {

    private final ThrottleConfig config;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public LogThrottler(ThrottleConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ThrottleConfig must not be null");
        }
        this.config = config;
    }

    /**
     * Determines whether the given log entry should be allowed through or throttled.
     *
     * @param entry the log entry to evaluate
     * @return true if the entry is allowed, false if it should be suppressed
     */
    public boolean allow(LogEntry entry) {
        if (entry == null) {
            return false;
        }
        if (!config.isEnabled()) {
            return true;
        }
        String key = buildKey(entry);
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());
        return counter.tryIncrement(config.getMaxEntriesPerWindow(), config.getWindowMillis());
    }

    /**
     * Resets throttle state for all tracked keys. Useful for testing or reconfiguration.
     */
    public void reset() {
        counters.clear();
    }

    /**
     * Returns the number of distinct throttle keys currently tracked.
     */
    public int trackedKeyCount() {
        return counters.size();
    }

    private String buildKey(LogEntry entry) {
        String service = entry.getService() != null ? entry.getService() : "unknown";
        String level = entry.getLevel() != null ? entry.getLevel() : "UNKNOWN";
        return service + ":" + level;
    }

    private static class WindowCounter {
        private final AtomicLong count = new AtomicLong(0);
        private volatile long windowStart = Instant.now().toEpochMilli();

        synchronized boolean tryIncrement(long maxEntries, long windowMillis) {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart >= windowMillis) {
                windowStart = now;
                count.set(0);
            }
            long current = count.incrementAndGet();
            return current <= maxEntries;
        }
    }
}
