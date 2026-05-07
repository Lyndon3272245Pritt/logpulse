package com.logpulse.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for the LogDispatcher, defining targets and dispatch strategy.
 */
public class DispatchConfig {

    public enum Strategy {
        BROADCAST,   // send to all targets
        ROUND_ROBIN, // rotate among targets
        FIRST_MATCH  // send to first target whose predicate matches
    }

    private final Strategy strategy;
    private final List<String> targetNames;
    private final boolean failFast;
    private final int maxRetries;

    private DispatchConfig(Builder builder) {
        this.strategy = builder.strategy;
        this.targetNames = List.copyOf(builder.targetNames);
        this.failFast = builder.failFast;
        this.maxRetries = builder.maxRetries;
    }

    public Strategy getStrategy() { return strategy; }
    public List<String> getTargetNames() { return targetNames; }
    public boolean isFailFast() { return failFast; }
    public int getMaxRetries() { return maxRetries; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Strategy strategy = Strategy.BROADCAST;
        private final List<String> targetNames = new ArrayList<>();
        private boolean failFast = false;
        private int maxRetries = 0;

        public Builder strategy(Strategy strategy) {
            this.strategy = Objects.requireNonNull(strategy);
            return this;
        }
        public Builder addTarget(String name) {
            this.targetNames.add(Objects.requireNonNull(name));
            return this;
        }
        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be >= 0");
            this.maxRetries = maxRetries;
            return this;
        }
        public DispatchConfig build() {
            if (targetNames.isEmpty()) throw new IllegalStateException("At least one target required");
            return new DispatchConfig(this);
        }
    }
}
