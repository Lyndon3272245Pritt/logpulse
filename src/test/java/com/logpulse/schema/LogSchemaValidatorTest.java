package com.logpulse.schema;

import com.logpulse.model.LogEntry;
import com.logpulse.schema.LogSchemaValidator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LogSchemaValidatorTest {

    private SchemaConfig config;

    @BeforeEach
    void setUp() {
        config = new SchemaConfig();
        config.setRequiredFields(Set.of("level", "service", "message"));
        config.setAllowedLevels(Set.of("DEBUG", "INFO", "WARN", "ERROR"));
        config.addFieldPattern("service", "[a-zA-Z0-9_-]+");
    }

    private LogEntry buildEntry(String level, String service, String message) {
        LogEntry entry = new LogEntry();
        entry.setLevel(level);
        entry.setService(service);
        entry.setMessage(message);
        entry.setTimestamp(Instant.now());
        return entry;
    }

    @Test
    void validEntry_shouldPassValidation() {
        LogSchemaValidator validator = new LogSchemaValidator(config);
        LogEntry entry = buildEntry("INFO", "auth-service", "User logged in");
        ValidationResult result = validator.validate(entry);
        assertTrue(result.isValid());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void missingRequiredField_shouldFailValidation() {
        LogSchemaValidator validator = new LogSchemaValidator(config);
        LogEntry entry = buildEntry("INFO", null, "Some message");
        ValidationResult result = validator.validate(entry);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("service")));
    }

    @Test
    void invalidLogLevel_shouldFailValidation() {
        LogSchemaValidator validator = new LogSchemaValidator(config);
        LogEntry entry = buildEntry("TRACE", "payment-service", "Processing payment");
        ValidationResult result = validator.validate(entry);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("TRACE")));
    }

    @Test
    void serviceNameWithSpaces_shouldFailPatternCheck() {
        LogSchemaValidator validator = new LogSchemaValidator(config);
        LogEntry entry = buildEntry("ERROR", "bad service name", "Crash");
        ValidationResult result = validator.validate(entry);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("service")));
    }

    @Test
    void nullEntry_shouldReturnInvalidResult() {
        LogSchemaValidator validator = new LogSchemaValidator(config);
        ValidationResult result = validator.validate(null);
        assertFalse(result.isValid());
        assertFalse(result.getViolations().isEmpty());
    }

    @Test
    void nullConfig_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new LogSchemaValidator(null));
    }

    @Test
    void extraFieldsInEntry_shouldPassIfRequiredFieldsPresent() {
        LogSchemaValidator validator = new LogSchemaValidator(config);
        LogEntry entry = buildEntry("WARN", "inventory", "Low stock");
        entry.setFields(Map.of("region", "us-east-1", "requestId", "abc-123"));
        ValidationResult result = validator.validate(entry);
        assertTrue(result.isValid());
    }

    @Test
    void emptyAllowedLevels_shouldSkipLevelCheck() {
        config.setAllowedLevels(Set.of());
        LogSchemaValidator validator = new LogSchemaValidator(config);
        LogEntry entry = buildEntry("VERBOSE", "debug-svc", "Detailed trace");
        ValidationResult result = validator.validate(entry);
        assertTrue(result.isValid());
    }
}
