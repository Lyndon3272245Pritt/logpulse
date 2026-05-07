package com.logpulse.masking;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogMaskerTest {

    private static final Instant TS = Instant.parse("2024-06-01T10:00:00Z");

    private MaskingConfig config;
    private LogMasker masker;

    @BeforeEach
    void setUp() {
        config = MaskingConfig.builder()
                .addRule("password", "password=[^\\s&]+", "password=[REDACTED]")
                .addRule("email", "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")
                .build();
        masker = new LogMasker(config);
    }

    private LogEntry entry(String message, Map<String, String> fields) {
        return new LogEntry(TS, "INFO", "auth-service", message, fields);
    }

    @Test
    void masksPasswordInMessage() {
        LogEntry result = masker.mask(entry("login attempt password=secret123", null));
        assertEquals("login attempt password=[REDACTED]", result.getMessage());
    }

    @Test
    void masksEmailInMessage() {
        LogEntry result = masker.mask(entry("user user@example.com logged in", null));
        assertEquals("user [REDACTED] logged in", result.getMessage());
    }

    @Test
    void masksFieldValues() {
        Map<String, String> fields = new HashMap<>();
        fields.put("user", "admin@corp.io");
        fields.put("action", "delete");
        LogEntry result = masker.mask(entry("action performed", fields));
        assertEquals("[REDACTED]", result.getFields().get("user"));
        assertEquals("delete", result.getFields().get("action"));
    }

    @Test
    void returnsOriginalWhenGloballyDisabled() {
        MaskingConfig disabled = MaskingConfig.builder()
                .globallyEnabled(false)
                .addRule("password", "password=[^\\s&]+", "password=[REDACTED]")
                .build();
        LogMasker disabledMasker = new LogMasker(disabled);
        LogEntry original = entry("password=topsecret", null);
        assertSame(original, disabledMasker.mask(original));
    }

    @Test
    void returnsNullForNullEntry() {
        assertNull(masker.mask(null));
    }

    @Test
    void originalEntryIsNotMutated() {
        LogEntry original = entry("token password=abc123", null);
        String originalMessage = original.getMessage();
        masker.mask(original);
        assertEquals(originalMessage, original.getMessage());
    }

    @Test
    void noRulesReturnsOriginalEntry() {
        LogMasker noRules = new LogMasker(MaskingConfig.builder().build());
        LogEntry original = entry("password=secret", null);
        assertSame(original, noRules.mask(original));
    }
}
