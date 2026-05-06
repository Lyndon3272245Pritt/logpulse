package com.logpulse.ratelimit;

import com.logpulse.model.LogEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter that restricts log throughput per service within a sliding time window.
 * Entries exceeding the configured limit are either dropped or passed through
 * depending on {@link RateLimiterConfig#isDropOnExceed()}.
 */
public class LogRateLimiter {

    private final RateLimiterConfig config;

    private static class WindowState {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart;

        WindowState(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    private final Map<String, WindowState> windowMap = new ConcurrentHashMap<>();

    public LogRateLimiter(RateLimiterConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
    }

    /**
     * Determines whether the given log entry should be allowed through.
     *
     * @param entry the log entry to evaluate
     * @return true if the entry is within the rate limit, false if it should be dropped
     */
    public boolean allow(LogEntry entry) {
        if (entry == null) return false;
        String service = entry.getService();
        long now = System.currentTimeMillis();

        WindowState state = windowMap.compute(service, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) >= config.getWindowDurationMillis()) {
                WindowState fresh = new WindowState(now);
                fresh.count.set(1);
                return fresh;
            }
            return existing;
        });

        // If we just reset the window, count is already 1 and we allow it
        if (state.count.get() == 1 && (now - state.windowStart) < config.getWindowDurationMillis()) {
            // Could be a fresh window; increment only if not already counted
        }

        int current = state.count.get();
        if (current > 1) {
            // Window was not just reset; increment and check
            int incremented = state.count.incrementAndGet();
            return !config.isDropOnExceed() || incremented <= config.getMaxEntriesPerWindow();
        }
        // count == 1 means window was just created with this entry
        return true;
    }

    /**
     * Returns the current entry count for a given service in the active window.
     */
    public int getCurrentCount(String service) {
        WindowState state = windowMap.get(service);
        if (state == null) return 0;
        long now = System.currentTimeMillis();
        if ((now - state.windowStart) >= config.getWindowDurationMillis()) return 0;
        return state.count.get();
    }

    /** Clears all tracked window state. Useful for testing. */
    public void reset() {
        windowMap.clear();
    }
}
