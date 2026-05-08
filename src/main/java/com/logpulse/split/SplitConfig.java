package com.logpulse.split;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for log splitting behaviour — defines how a single log stream
 * is fanned out into multiple named output streams based on field values or
 * patterns.
 */
public class SplitConfig {

    public enum SplitStrategy {
        /** Split on the value of a named field (e.g. "service", "level"). */
        FIELD_VALUE,
        /** Split using a regex applied to the raw log message. */
        PATTERN
    }

    private SplitStrategy strategy = SplitStrategy.FIELD_VALUE;
    private String splitField = "service";
    private String splitPattern;
    private int maxBuckets = 64;
    private boolean dropUnmatched = false;
    private String unmatchedBucket = "__unmatched__";
    private List<String> allowedBuckets = new ArrayList<>();

    // ------------------------------------------------------------------ //

    public SplitStrategy getStrategy() { return strategy; }
    public SplitConfig setStrategy(SplitStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
        return this;
    }

    public String getSplitField() { return splitField; }
    public SplitConfig setSplitField(String splitField) {
        this.splitField = Objects.requireNonNull(splitField);
        return this;
    }

    public String getSplitPattern() { return splitPattern; }
    public SplitConfig setSplitPattern(String splitPattern) {
        this.splitPattern = splitPattern;
        return this;
    }

    public int getMaxBuckets() { return maxBuckets; }
    public SplitConfig setMaxBuckets(int maxBuckets) {
        if (maxBuckets < 1) throw new IllegalArgumentException("maxBuckets must be >= 1");
        this.maxBuckets = maxBuckets;
        return this;
    }

    public boolean isDropUnmatched() { return dropUnmatched; }
    public SplitConfig setDropUnmatched(boolean dropUnmatched) {
        this.dropUnmatched = dropUnmatched;
        return this;
    }

    public String getUnmatchedBucket() { return unmatchedBucket; }
    public SplitConfig setUnmatchedBucket(String unmatchedBucket) {
        this.unmatchedBucket = Objects.requireNonNull(unmatchedBucket);
        return this;
    }

    public List<String> getAllowedBuckets() { return allowedBuckets; }
    public SplitConfig setAllowedBuckets(List<String> allowedBuckets) {
        this.allowedBuckets = Objects.requireNonNull(allowedBuckets);
        return this;
    }

    @Override
    public String toString() {
        return "SplitConfig{strategy=" + strategy +
               ", splitField='" + splitField + "'" +
               ", maxBuckets=" + maxBuckets +
               ", dropUnmatched=" + dropUnmatched + "}";
    }
}
