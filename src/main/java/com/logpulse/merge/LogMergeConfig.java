package com.logpulse.merge;

import java.util.Comparator;

/**
 * Configuration for merging log streams from multiple sources
 * into a single chronologically ordered stream.
 */
public class LogMergeConfig {

    public enum MergeStrategy {
        TIMESTAMP_ASC,
        TIMESTAMP_DESC,
        ROUND_ROBIN,
        PRIORITY
    }

    private MergeStrategy strategy;
    private long timestampToleranceMs;
    private boolean dropDuplicates;
    private int maxSources;
    private String timestampField;

    private LogMergeConfig() {
        this.strategy = MergeStrategy.TIMESTAMP_ASC;
        this.timestampToleranceMs = 0L;
        this.dropDuplicates = false;
        this.maxSources = 16;
        this.timestampField = "timestamp";
    }

    public static LogMergeConfig defaults() {
        return new LogMergeConfig();
    }

    public LogMergeConfig withStrategy(MergeStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy must not be null");
        this.strategy = strategy;
        return this;
    }

    public LogMergeConfig withTimestampToleranceMs(long toleranceMs) {
        if (toleranceMs < 0) throw new IllegalArgumentException("toleranceMs must be >= 0");
        this.timestampToleranceMs = toleranceMs;
        return this;
    }

    public LogMergeConfig withDropDuplicates(boolean dropDuplicates) {
        this.dropDuplicates = dropDuplicates;
        return this;
    }

    public LogMergeConfig withMaxSources(int maxSources) {
        if (maxSources < 1) throw new IllegalArgumentException("maxSources must be >= 1");
        this.maxSources = maxSources;
        return this;
    }

    public LogMergeConfig withTimestampField(String timestampField) {
        if (timestampField == null || timestampField.isBlank())
            throw new IllegalArgumentException("timestampField must not be blank");
        this.timestampField = timestampField;
        return this;
    }

    public MergeStrategy getStrategy() { return strategy; }
    public long getTimestampToleranceMs() { return timestampToleranceMs; }
    public boolean isDropDuplicates() { return dropDuplicates; }
    public int getMaxSources() { return maxSources; }
    public String getTimestampField() { return timestampField; }

    @Override
    public String toString() {
        return "LogMergeConfig{strategy=" + strategy
                + ", toleranceMs=" + timestampToleranceMs
                + ", dropDuplicates=" + dropDuplicates
                + ", maxSources=" + maxSources
                + ", timestampField='" + timestampField + "'}";
    }
}
