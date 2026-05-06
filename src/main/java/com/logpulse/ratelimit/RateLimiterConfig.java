package com.logpulse.ratelimit;

/**
 * Configuration for the log rate limiter.
 * Controls how many log entries are allowed per service per time window.
 */
public class RateLimiterConfig {

    private final int maxEntriesPerWindow;
    private final long windowDurationMillis;
    private final boolean dropOnExceed;

    private RateLimiterConfig(Builder builder) {
        this.maxEntriesPerWindow = builder.maxEntriesPerWindow;
        this.windowDurationMillis = builder.windowDurationMillis;
        this.dropOnExceed = builder.dropOnExceed;
    }

    public int getMaxEntriesPerWindow() {
        return maxEntriesPerWindow;
    }

    public long getWindowDurationMillis() {
        return windowDurationMillis;
    }

    public boolean isDropOnExceed() {
        return dropOnExceed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxEntriesPerWindow = 100;
        private long windowDurationMillis = 1000L;
        private boolean dropOnExceed = true;

        public Builder maxEntriesPerWindow(int max) {
            if (max <= 0) throw new IllegalArgumentException("maxEntriesPerWindow must be positive");
            this.maxEntriesPerWindow = max;
            return this;
        }

        public Builder windowDurationMillis(long millis) {
            if (millis <= 0) throw new IllegalArgumentException("windowDurationMillis must be positive");
            this.windowDurationMillis = millis;
            return this;
        }

        public Builder dropOnExceed(boolean drop) {
            this.dropOnExceed = drop;
            return this;
        }

        public RateLimiterConfig build() {
            return new RateLimiterConfig(this);
        }
    }
}
