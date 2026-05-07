package com.logpulse.redact;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogRedactorTest {

    private LogEntry sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = new LogEntry(
                Instant.parse("2024-06-01T10:00:00Z"),
                "INFO",
                "auth-service",
                "User logged in with token=abc123secret and card=4111111111111111",
                Map.of("requestId", "req-99")
        );
    }

    @Test
    void redact_withNoRules_returnsOriginalEntry() {
        LogRedactor redactor = new LogRedactor(Collections.emptyList());
        LogEntry result = redactor.redact(sampleEntry);
        assertSame(sampleEntry, result);
    }

    @Test
    void redact_withNullEntry_returnsNull() {
        LogRedactor redactor = new LogRedactor(List.of(
                new RedactRule("token", "token=\\S+", "[TOKEN]"))
        );
        assertNull(redactor.redact(null));
    }

    @Test
    void redact_replacesTokenPattern() {
        RedactRule tokenRule = new RedactRule("token", "token=\\S+", "token=[TOKEN]");
        LogRedactor redactor = new LogRedactor(List.of(tokenRule));

        LogEntry result = redactor.redact(sampleEntry);

        assertTrue(result.getMessage().contains("token=[TOKEN]"),
                "Expected token to be redacted");
        assertFalse(result.getMessage().contains("abc123secret"),
                "Raw token value should be gone");
    }

    @Test
    void redact_replacesMultiplePatterns() {
        RedactRule tokenRule = new RedactRule("token", "token=\\S+", "token=[TOKEN]");
        RedactRule cardRule  = new RedactRule("card",  "\\b\\d{16}\\b",  "[CARD]");
        LogRedactor redactor = new LogRedactor(List.of(tokenRule, cardRule));

        LogEntry result = redactor.redact(sampleEntry);

        assertTrue(result.getMessage().contains("token=[TOKEN]"));
        assertTrue(result.getMessage().contains("[CARD]"));
        assertFalse(result.getMessage().contains("4111111111111111"));
    }

    @Test
    void redact_preservesMetadata() {
        RedactRule rule = new RedactRule("token", "token=\\S+", "[TOKEN]");
        LogRedactor redactor = new LogRedactor(List.of(rule));

        LogEntry result = redactor.redact(sampleEntry);

        assertEquals(sampleEntry.getTimestamp(), result.getTimestamp());
        assertEquals(sampleEntry.getLevel(),     result.getLevel());
        assertEquals(sampleEntry.getService(),   result.getService());
        assertEquals(sampleEntry.getFields(),    result.getFields());
    }

    @Test
    void redactAll_processesAllEntries() {
        RedactRule rule = new RedactRule("secret", "secret", "[SECRET]");
        LogRedactor redactor = new LogRedactor(List.of(rule));

        LogEntry e1 = new LogEntry(Instant.now(), "WARN", "svc", "no match here", Map.of());
        LogEntry e2 = new LogEntry(Instant.now(), "ERROR", "svc", "secret value exposed", Map.of());

        List<LogEntry> results = redactor.redactAll(List.of(e1, e2));

        assertEquals(2, results.size());
        assertSame(e1, results.get(0), "Unchanged entry should be same reference");
        assertTrue(results.get(1).getMessage().contains("[SECRET]"));
    }

    @Test
    void redactRule_throwsOnBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new RedactRule("", "pattern", "[X]"));
    }

    @Test
    void redactRule_defaultReplacement() {
        RedactRule rule = new RedactRule("pw", "password=\\S+", null);
        String result = rule.apply("login password=hunter2 failed");
        assertTrue(result.contains("[REDACTED]"));
    }
}
