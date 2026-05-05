package com.logpulse.sampling;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration for {@link LogSampler}.
 * Controls the sampling rate and which log levels are always forwarded.
 */
public class SamplerConfig {

    public enum Level { TRACE, DEBUG, INFO, WARN, ERROR, FATAL }

    private final int sampleRate;
    private final Set<Level> forcedLevels;

    private SamplerConfig(Builder builder) {
        this.sampleRate = builder.sampleRate;
        this.forcedLevels = Collections.unmodifiableSet(
                builder.forcedLevels.isEmpty()
                        ? EnumSet.noneOf(Level.class)
                        : EnumSet.copyOf(builder.forcedLevels));
    }

    /** Returns N such that every N-th entry is accepted (1 = all entries). */
    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isForcedLevel(String levelName) {
        if (levelName == null || levelName.isBlank()) {
            return false;
        }
        try {
            return forcedLevels.contains(Level.valueOf(levelName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Set<Level> getForcedLevels() {
        return forcedLevels;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int sampleRate = 1;
        private final Set<Level> forcedLevels = EnumSet.noneOf(Level.class);

        /** Accept every {@code n}-th log entry. Must be >= 1. */
        public Builder sampleRate(int n) {
            if (n < 1) throw new IllegalArgumentException("sampleRate must be >= 1");
            this.sampleRate = n;
            return this;
        }

        /** Levels that bypass sampling and are always forwarded. */
        public Builder forceLevel(Level level) {
            this.forcedLevels.add(level);
            return this;
        }

        public SamplerConfig build() {
            return new SamplerConfig(this);
        }
    }
}
