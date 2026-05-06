package com.logpulse.buffer;

/**
 * Immutable configuration for {@link LogBuffer}.
 */
public class BufferConfig {

    public static final int DEFAULT_CAPACITY = 1024;
    public static final long DEFAULT_DRAIN_TIMEOUT_MS = 100L;

    private final int capacity;
    private final long drainTimeoutMs;

    private BufferConfig(Builder builder) {
        this.capacity = builder.capacity;
        this.drainTimeoutMs = builder.drainTimeoutMs;
    }

    public int getCapacity() {
        return capacity;
    }

    public long getDrainTimeoutMs() {
        return drainTimeoutMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BufferConfig defaults() {
        return builder().build();
    }

    public static class Builder {
        private int capacity = DEFAULT_CAPACITY;
        private long drainTimeoutMs = DEFAULT_DRAIN_TIMEOUT_MS;

        public Builder capacity(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
            this.capacity = capacity;
            return this;
        }

        public Builder drainTimeoutMs(long drainTimeoutMs) {
            if (drainTimeoutMs < 0) throw new IllegalArgumentException("Drain timeout must be non-negative");
            this.drainTimeoutMs = drainTimeoutMs;
            return this;
        }

        public BufferConfig build() {
            return new BufferConfig(this);
        }
    }

    @Override
    public String toString() {
        return "BufferConfig{capacity=" + capacity + ", drainTimeoutMs=" + drainTimeoutMs + "}";
    }
}
