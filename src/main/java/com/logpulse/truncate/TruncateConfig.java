package com.logpulse.truncate;

/**
 * Configuration for log message truncation.
 * Controls maximum field lengths and truncation behavior.
 */
public class TruncateConfig {

    public static final int DEFAULT_MAX_MESSAGE_LENGTH = 1024;
    public static final int DEFAULT_MAX_FIELD_LENGTH = 256;
    public static final String DEFAULT_SUFFIX = "...";

    private final int maxMessageLength;
    private final int maxFieldLength;
    private final String truncationSuffix;
    private final boolean truncateFields;
    private final boolean enabled;

    private TruncateConfig(Builder builder) {
        this.maxMessageLength = builder.maxMessageLength;
        this.maxFieldLength = builder.maxFieldLength;
        this.truncationSuffix = builder.truncationSuffix;
        this.truncateFields = builder.truncateFields;
        this.enabled = builder.enabled;
    }

    public int getMaxMessageLength() { return maxMessageLength; }
    public int getMaxFieldLength() { return maxFieldLength; }
    public String getTruncationSuffix() { return truncationSuffix; }
    public boolean isTruncateFields() { return truncateFields; }
    public boolean isEnabled() { return enabled; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int maxMessageLength = DEFAULT_MAX_MESSAGE_LENGTH;
        private int maxFieldLength = DEFAULT_MAX_FIELD_LENGTH;
        private String truncationSuffix = DEFAULT_SUFFIX;
        private boolean truncateFields = false;
        private boolean enabled = true;

        public Builder maxMessageLength(int maxMessageLength) {
            if (maxMessageLength <= 0) throw new IllegalArgumentException("maxMessageLength must be positive");
            this.maxMessageLength = maxMessageLength;
            return this;
        }

        public Builder maxFieldLength(int maxFieldLength) {
            if (maxFieldLength <= 0) throw new IllegalArgumentException("maxFieldLength must be positive");
            this.maxFieldLength = maxFieldLength;
            return this;
        }

        public Builder truncationSuffix(String truncationSuffix) {
            if (truncationSuffix == null) throw new IllegalArgumentException("truncationSuffix must not be null");
            this.truncationSuffix = truncationSuffix;
            return this;
        }

        public Builder truncateFields(boolean truncateFields) {
            this.truncateFields = truncateFields;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public TruncateConfig build() { return new TruncateConfig(this); }
    }
}
