package com.logpulse.truncate;

import com.logpulse.model.LogEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Truncates oversized log messages and/or individual fields
 * according to a {@link TruncateConfig}.
 */
public class LogTruncator {

    private final TruncateConfig config;

    public LogTruncator(TruncateConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Returns a (possibly new) LogEntry with truncated content.
     * If truncation is disabled the original entry is returned unchanged.
     */
    public LogEntry truncate(LogEntry entry) {
        if (entry == null || !config.isEnabled()) {
            return entry;
        }

        String message = entry.getMessage();
        String truncatedMessage = truncateValue(message, config.getMaxMessageLength());

        Map<String, String> fields = entry.getFields();
        Map<String, String> truncatedFields = fields;

        if (config.isTruncateFields() && fields != null && !fields.isEmpty()) {
            truncatedFields = new HashMap<>(fields.size());
            for (Map.Entry<String, String> kv : fields.entrySet()) {
                truncatedFields.put(kv.getKey(), truncateValue(kv.getValue(), config.getMaxFieldLength()));
            }
        }

        if (truncatedMessage == message && truncatedFields == fields) {
            return entry;
        }

        return LogEntry.builder()
                .timestamp(entry.getTimestamp())
                .level(entry.getLevel())
                .service(entry.getService())
                .message(truncatedMessage)
                .fields(truncatedFields)
                .build();
    }

    /**
     * Checks whether the given entry would be truncated without modifying it.
     */
    public boolean requiresTruncation(LogEntry entry) {
        if (entry == null || !config.isEnabled()) return false;
        if (entry.getMessage() != null && entry.getMessage().length() > config.getMaxMessageLength()) return true;
        if (config.isTruncateFields() && entry.getFields() != null) {
            for (String v : entry.getFields().values()) {
                if (v != null && v.length() > config.getMaxFieldLength()) return true;
            }
        }
        return false;
    }

    private String truncateValue(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        String suffix = config.getTruncationSuffix();
        int cutAt = Math.max(0, maxLength - suffix.length());
        return value.substring(0, cutAt) + suffix;
    }

    public TruncateConfig getConfig() { return config; }
}
