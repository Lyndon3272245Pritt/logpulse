package com.logpulse.watch;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for the LogWatcher — controls which paths to watch,
 * polling interval, and glob patterns for file inclusion/exclusion.
 */
public class WatchConfig {

    private final List<String> watchPaths;
    private final List<String> includePatterns;
    private final List<String> excludePatterns;
    private final Duration pollInterval;
    private final boolean recursive;

    private WatchConfig(Builder builder) {
        this.watchPaths = Collections.unmodifiableList(builder.watchPaths);
        this.includePatterns = Collections.unmodifiableList(builder.includePatterns);
        this.excludePatterns = Collections.unmodifiableList(builder.excludePatterns);
        this.pollInterval = builder.pollInterval;
        this.recursive = builder.recursive;
    }

    public List<String> getWatchPaths() { return watchPaths; }
    public List<String> getIncludePatterns() { return includePatterns; }
    public List<String> getExcludePatterns() { return excludePatterns; }
    public Duration getPollInterval() { return pollInterval; }
    public boolean isRecursive() { return recursive; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<String> watchPaths = Collections.emptyList();
        private List<String> includePatterns = List.of("*.log");
        private List<String> excludePatterns = Collections.emptyList();
        private Duration pollInterval = Duration.ofSeconds(2);
        private boolean recursive = false;

        public Builder watchPaths(List<String> paths) {
            this.watchPaths = Objects.requireNonNull(paths);
            return this;
        }
        public Builder includePatterns(List<String> patterns) {
            this.includePatterns = Objects.requireNonNull(patterns);
            return this;
        }
        public Builder excludePatterns(List<String> patterns) {
            this.excludePatterns = Objects.requireNonNull(patterns);
            return this;
        }
        public Builder pollInterval(Duration interval) {
            if (interval.isNegative() || interval.isZero()) {
                throw new IllegalArgumentException("pollInterval must be positive");
            }
            this.pollInterval = interval;
            return this;
        }
        public Builder recursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }
        public WatchConfig build() {
            if (watchPaths.isEmpty()) {
                throw new IllegalStateException("At least one watch path must be specified");
            }
            return new WatchConfig(this);
        }
    }
}
