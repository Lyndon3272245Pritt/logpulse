package com.logpulse.fanout;

/**
 * Configuration for {@link LogFanout}.
 */
public class FanoutConfig {

    /** When true, the first subscriber failure aborts fanout and rethrows. */
    private final boolean failFast;

    /** Maximum number of subscribers allowed (0 = unlimited). */
    private final int maxSubscribers;

    private FanoutConfig(Builder builder) {
        this.failFast = builder.failFast;
        this.maxSubscribers = builder.maxSubscribers;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public int getMaxSubscribers() {
        return maxSubscribers;
    }

    @Override
    public String toString() {
        return "FanoutConfig{failFast=" + failFast +
                ", maxSubscribers=" + maxSubscribers + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean failFast = false;
        private int maxSubscribers = 0;

        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public Builder maxSubscribers(int maxSubscribers) {
            if (maxSubscribers < 0) {
                throw new IllegalArgumentException("maxSubscribers must be >= 0");
            }
            this.maxSubscribers = maxSubscribers;
            return this;
        }

        public FanoutConfig build() {
            return new FanoutConfig(this);
        }
    }

    /** Convenience factory for a permissive default config. */
    public static FanoutConfig defaults() {
        return builder().build();
    }
}
