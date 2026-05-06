package com.logpulse.correlation;

/**
 * Configuration for the LogCorrelator.
 */
public class CorrelationConfig {

    private static final String DEFAULT_FIELD = "correlationId";
    private static final int DEFAULT_WINDOW_SECONDS = 60;
    private static final int MIN_WINDOW_SECONDS = 1;
    private static final int MAX_WINDOW_SECONDS = 3600;

    private final String correlationField;
    private final int windowSeconds;

    private CorrelationConfig(Builder builder) {
        this.correlationField = builder.correlationField;
        this.windowSeconds = builder.windowSeconds;
    }

    public String getCorrelationField() {
        return correlationField;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CorrelationConfig defaults() {
        return builder().build();
    }

    @Override
    public String toString() {
        return "CorrelationConfig{field='" + correlationField + "', windowSeconds=" + windowSeconds + "}";
    }

    public static class Builder {
        private String correlationField = DEFAULT_FIELD;
        private int windowSeconds = DEFAULT_WINDOW_SECONDS;

        public Builder correlationField(String field) {
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("Correlation field must not be blank");
            }
            this.correlationField = field;
            return this;
        }

        public Builder windowSeconds(int seconds) {
            if (seconds < MIN_WINDOW_SECONDS || seconds > MAX_WINDOW_SECONDS) {
                throw new IllegalArgumentException(
                        "windowSeconds must be between " + MIN_WINDOW_SECONDS + " and " + MAX_WINDOW_SECONDS);
            }
            this.windowSeconds = seconds;
            return this;
        }

        public CorrelationConfig build() {
            return new CorrelationConfig(this);
        }
    }
}
