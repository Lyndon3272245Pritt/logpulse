package com.logpulse.normalize;

import com.logpulse.model.LogEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Normalizes {@link LogEntry} fields according to a {@link NormalizeConfig}.
 * Applies field renaming, default injection, level casing, and whitespace trimming.
 */
public class LogNormalizer {

    private final NormalizeConfig config;

    public LogNormalizer(NormalizeConfig config) {
        this.config = Objects.requireNonNull(config, "NormalizeConfig must not be null");
    }

    /**
     * Normalizes the given log entry and returns a new instance with applied transformations.
     *
     * @param entry the original log entry
     * @return a normalized log entry, or null if the input is null
     */
    public LogEntry normalize(LogEntry entry) {
        if (entry == null) {
            return null;
        }

        String service = entry.getService();
        String level = entry.getLevel();
        String message = entry.getMessage();
        Map<String, String> fields = new HashMap<>(entry.getFields());

        // Trim whitespace on core fields
        if (config.isTrimWhitespace()) {
            if (service != null) service = service.strip();
            if (level != null) level = level.strip();
            if (message != null) message = message.strip();
            fields.replaceAll((k, v) -> v != null ? v.strip() : null);
        }

        // Normalize level casing
        if (config.isLowercaseLevel() && level != null) {
            level = level.toLowerCase();
        }

        // Apply field renames
        for (Map.Entry<String, String> rename : config.getFieldRenames().entrySet()) {
            if (fields.containsKey(rename.getKey())) {
                String value = fields.remove(rename.getKey());
                fields.put(rename.getValue(), value);
            }
        }

        // Inject defaults for missing or blank fields
        for (Map.Entry<String, String> def : config.getFieldDefaults().entrySet()) {
            fields.putIfAbsent(def.getKey(), def.getValue());
            if (fields.get(def.getKey()) == null || fields.get(def.getKey()).isBlank()) {
                fields.put(def.getKey(), def.getValue());
            }
        }

        return new LogEntry(entry.getTimestamp(), service, level, message, fields);
    }
}
