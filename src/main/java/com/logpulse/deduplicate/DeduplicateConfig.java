package com.logpulse.deduplicate;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for log deduplication based on message fingerprinting.
 */
public class DeduplicateConfig {

    private final Duration windowDuration;
    private final int maxCacheSize;
    private final boolean caseSensitive;
    private final boolean includeServiceInKey;

    private DeduplicateConfig(Builder builder) {
        this.windowDuration = builder.windowDuration;
        this.maxCacheSize = builder.maxCacheSize;
        this.caseSensitive = builder.caseSensitive;
        this.includeServiceInKey = builder.includeServiceInKey;
    }

    public Duration getWindowDuration() {
        return windowDuration;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isIncludeServiceInKey() {
        return includeServiceInKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration windowDuration = Duration.ofSeconds(30);
        private int maxCacheSize = 1000;
        private boolean caseSensitive = true;
        private boolean includeServiceInKey = true;

        public Builder windowDuration(Duration windowDuration) {
            Objects.requireNonNull(windowDuration, "windowDuration must not be null");
            if (windowDuration.isNegative() || windowDuration.isZero()) {
                throw new IllegalArgumentException("windowDuration must be positive");
            }
            this.windowDuration = windowDuration;
            return this;
        }

        public Builder maxCacheSize(int maxCacheSize) {
            if (maxCacheSize <= 0) {
                throw new IllegalArgumentException("maxCacheSize must be positive");
            }
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public Builder includeServiceInKey(boolean includeServiceInKey) {
            this.includeServiceInKey = includeServiceInKey;
            return this;
        }

        public DeduplicateConfig build() {
            return new DeduplicateConfig(this);
        }
    }
}
