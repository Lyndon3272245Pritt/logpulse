package com.logpulse.retry;

import java.time.Duration;

/**
 * Configuration for log dispatch retry behavior.
 */
public class RetryConfig {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final Duration maxDelay;
    private final boolean retryOnAllFailures;

    private RetryConfig(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelay = builder.initialDelay;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.maxDelay = builder.maxDelay;
        this.retryOnAllFailures = builder.retryOnAllFailures;
    }

    public int getMaxAttempts() { return maxAttempts; }
    public Duration getInitialDelay() { return initialDelay; }
    public double getBackoffMultiplier() { return backoffMultiplier; }
    public Duration getMaxDelay() { return maxDelay; }
    public boolean isRetryOnAllFailures() { return retryOnAllFailures; }

    public Duration delayForAttempt(int attempt) {
        long ms = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt - 1));
        return Duration.ofMillis(Math.min(ms, maxDelay.toMillis()));
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int maxAttempts = 3;
        private Duration initialDelay = Duration.ofMillis(200);
        private double backoffMultiplier = 2.0;
        private Duration maxDelay = Duration.ofSeconds(10);
        private boolean retryOnAllFailures = false;

        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
            this.maxAttempts = maxAttempts;
            return this;
        }
        public Builder initialDelay(Duration initialDelay) { this.initialDelay = initialDelay; return this; }
        public Builder backoffMultiplier(double multiplier) { this.backoffMultiplier = multiplier; return this; }
        public Builder maxDelay(Duration maxDelay) { this.maxDelay = maxDelay; return this; }
        public Builder retryOnAllFailures(boolean retryOnAllFailures) { this.retryOnAllFailures = retryOnAllFailures; return this; }

        public RetryConfig build() { return new RetryConfig(this); }
    }
}
