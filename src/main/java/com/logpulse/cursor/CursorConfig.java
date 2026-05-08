package com.logpulse.cursor;

import java.time.Duration;

/**
 * Configuration for log cursor tracking — maintains per-source read positions
 * to support resumable tailing across restarts.
 */
public class CursorConfig {

    private final String storePath;
    private final boolean persistOnShutdown;
    private final Duration flushInterval;
    private final boolean autoResumeOnStart;
    private final int maxTrackedSources;

    private CursorConfig(Builder builder) {
        this.storePath = builder.storePath;
        this.persistOnShutdown = builder.persistOnShutdown;
        this.flushInterval = builder.flushInterval;
        this.autoResumeOnStart = builder.autoResumeOnStart;
        this.maxTrackedSources = builder.maxTrackedSources;
    }

    public String getStorePath() { return storePath; }
    public boolean isPersistOnShutdown() { return persistOnShutdown; }
    public Duration getFlushInterval() { return flushInterval; }
    public boolean isAutoResumeOnStart() { return autoResumeOnStart; }
    public int getMaxTrackedSources() { return maxTrackedSources; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String storePath = ".logpulse/cursors";
        private boolean persistOnShutdown = true;
        private Duration flushInterval = Duration.ofSeconds(30);
        private boolean autoResumeOnStart = true;
        private int maxTrackedSources = 256;

        public Builder storePath(String storePath) {
            if (storePath == null || storePath.isBlank()) throw new IllegalArgumentException("storePath must not be blank");
            this.storePath = storePath;
            return this;
        }
        public Builder persistOnShutdown(boolean persist) { this.persistOnShutdown = persist; return this; }
        public Builder flushInterval(Duration interval) {
            if (interval == null || interval.isNegative()) throw new IllegalArgumentException("flushInterval must be positive");
            this.flushInterval = interval;
            return this;
        }
        public Builder autoResumeOnStart(boolean resume) { this.autoResumeOnStart = resume; return this; }
        public Builder maxTrackedSources(int max) {
            if (max <= 0) throw new IllegalArgumentException("maxTrackedSources must be > 0");
            this.maxTrackedSources = max;
            return this;
        }
        public CursorConfig build() { return new CursorConfig(this); }
    }
}
