package com.logpulse.schema;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates LogEntry instances against a configurable schema.
 * Supports required field checks, type constraints, and regex patterns.
 */
public class LogSchemaValidator {

    private final SchemaConfig config;

    public LogSchemaValidator(SchemaConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SchemaConfig must not be null");
        }
        this.config = config;
    }

    /**
     * Validates a LogEntry against the configured schema.
     *
     * @param entry the log entry to validate
     * @return a ValidationResult containing any violations found
     */
    public ValidationResult validate(LogEntry entry) {
        List<String> violations = new ArrayList<>();

        if (entry == null) {
            violations.add("LogEntry must not be null");
            return new ValidationResult(false, violations);
        }

        // Check required fields
        for (String required : config.getRequiredFields()) {
            Object value = resolveField(entry, required);
            if (value == null || value.toString().isBlank()) {
                violations.add("Missing required field: " + required);
            }
        }

        // Check field patterns
        for (Map.Entry<String, Pattern> patternEntry : config.getFieldPatterns().entrySet()) {
            String field = patternEntry.getKey();
            Pattern pattern = patternEntry.getValue();
            Object value = resolveField(entry, field);
            if (value != null && !pattern.matcher(value.toString()).matches()) {
                violations.add("Field '" + field + "' value '" + value + "' does not match pattern: " + pattern.pattern());
            }
        }

        // Check allowed levels
        Set<String> allowedLevels = config.getAllowedLevels();
        if (!allowedLevels.isEmpty() && entry.getLevel() != null) {
            if (!allowedLevels.contains(entry.getLevel().toUpperCase())) {
                violations.add("Invalid log level: '" + entry.getLevel() + "'. Allowed: " + allowedLevels);
            }
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }

    private Object resolveField(LogEntry entry, String field) {
        switch (field) {
            case "level":   return entry.getLevel();
            case "service": return entry.getService();
            case "message": return entry.getMessage();
            case "timestamp": return entry.getTimestamp();
            default:
                Map<String, String> fields = entry.getFields();
                return fields != null ? fields.get(field) : null;
        }
    }

    public SchemaConfig getConfig() {
        return config;
    }

    /** Immutable result of a schema validation. */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> violations;

        public ValidationResult(boolean valid, List<String> violations) {
            this.valid = valid;
            this.violations = List.copyOf(violations);
        }

        public boolean isValid() { return valid; }
        public List<String> getViolations() { return violations; }

        @Override
        public String toString() {
            return valid ? "ValidationResult{VALID}" : "ValidationResult{INVALID, violations=" + violations + "}";
        }
    }
}
