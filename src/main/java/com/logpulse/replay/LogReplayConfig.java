package com.logpulse.replay;

import java.time.Instant;

/**
 * Configuration for replaying historical log entries from a file or buffer.
 */
public class LogReplayConfig {

    private final String sourcePath;
    private final Instant fromTime;
    private final Instant toTime;
    private final double speedMultiplier;
    private final boolean loop;
    private final int maxEntriesPerSecond;

    private LogReplayConfig(Builder builder) {
        this.sourcePath = builder.sourcePath;
        this.fromTime = builder.fromTime;
        this.toTime = builder.toTime;
        this.speedMultiplier = builder.speedMultiplier;
        this.loop = builder.loop;
        this.maxEntriesPerSecond = builder.maxEntriesPerSecond;
    }

    public String getSourcePath() { return sourcePath; }
    public Instant getFromTime() { return fromTime; }
    public Instant getToTime() { return toTime; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public boolean isLoop() { return loop; }
    public int getMaxEntriesPerSecond() { return maxEntriesPerSecond; }

    public static Builder builder(String sourcePath) {
        return new Builder(sourcePath);
    }

    public static class Builder {
        private final String sourcePath;
        private Instant fromTime = Instant.EPOCH;
        private Instant toTime = Instant.MAX;
        private double speedMultiplier = 1.0;
        private boolean loop = false;
        private int maxEntriesPerSecond = Integer.MAX_VALUE;

        private Builder(String sourcePath) {
            if (sourcePath == null || sourcePath.isBlank()) {
                throw new IllegalArgumentException("sourcePath must not be blank");
            }
            this.sourcePath = sourcePath;
        }

        public Builder fromTime(Instant fromTime) { this.fromTime = fromTime; return this; }
        public Builder toTime(Instant toTime) { this.toTime = toTime; return this; }
        public Builder speedMultiplier(double speedMultiplier) {
            if (speedMultiplier <= 0) throw new IllegalArgumentException("speedMultiplier must be positive");
            this.speedMultiplier = speedMultiplier;
            return this;
        }
        public Builder loop(boolean loop) { this.loop = loop; return this; }
        public Builder maxEntriesPerSecond(int max) {
            if (max <= 0) throw new IllegalArgumentException("maxEntriesPerSecond must be positive");
            this.maxEntriesPerSecond = max;
            return this;
        }

        public LogReplayConfig build() { return new LogReplayConfig(this); }
    }
}
