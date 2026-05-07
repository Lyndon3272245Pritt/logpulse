package com.logpulse.masking;

import com.logpulse.model.LogEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Applies configured {@link MaskingRule}s to {@link LogEntry} instances,
 * masking sensitive fields such as passwords, tokens, and PII before
 * the entries are forwarded to output sinks.
 */
public class LogMasker {

    private final MaskingConfig config;

    public LogMasker(MaskingConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Returns a new {@link LogEntry} with sensitive data masked.
     * The original entry is never mutated.
     *
     * @param entry source log entry
     * @return masked log entry, or the original if masking is globally disabled
     */
    public LogEntry mask(LogEntry entry) {
        if (entry == null || !config.isGloballyEnabled() || config.getRules().isEmpty()) {
            return entry;
        }

        String maskedMessage = applyRules(entry.getMessage());
        Map<String, String> maskedFields = maskFields(entry.getFields());

        return new LogEntry(
                entry.getTimestamp(),
                entry.getLevel(),
                entry.getService(),
                maskedMessage,
                maskedFields
        );
    }

    private String applyRules(String value) {
        if (value == null) return null;
        String result = value;
        for (MaskingRule rule : config.getRules()) {
            result = rule.apply(result);
        }
        return result;
    }

    private Map<String, String> maskFields(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return fields;
        }
        Map<String, String> masked = new HashMap<>(fields.size());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            masked.put(entry.getKey(), applyRules(entry.getValue()));
        }
        return masked;
    }

    public MaskingConfig getConfig() {
        return config;
    }
}
