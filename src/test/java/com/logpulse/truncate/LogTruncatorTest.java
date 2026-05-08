package com.logpulse.truncate;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogTruncatorTest {

    private static final Instant NOW = Instant.now();

    private LogEntry entryWithMessage(String message) {
        return LogEntry.builder()
                .timestamp(NOW)
                .level("INFO")
                .service("svc")
                .message(message)
                .fields(new HashMap<>())
                .build();
    }

    private LogEntry entryWithFields(Map<String, String> fields) {
        return LogEntry.builder()
                .timestamp(NOW)
                .level("INFO")
                .service("svc")
                .message("short")
                .fields(fields)
                .build();
    }

    @Test
    void shortMessageIsUnchanged() {
        TruncateConfig cfg = TruncateConfig.builder().maxMessageLength(100).build();
        LogTruncator truncator = new LogTruncator(cfg);
        LogEntry entry = entryWithMessage("hello");
        assertSame(entry, truncator.truncate(entry));
    }

    @Test
    void longMessageIsTruncated() {
        TruncateConfig cfg = TruncateConfig.builder().maxMessageLength(10).truncationSuffix("...").build();
        LogTruncator truncator = new LogTruncator(cfg);
        LogEntry entry = entryWithMessage("this is a very long message");
        LogEntry result = truncator.truncate(entry);
        assertNotSame(entry, result);
        assertEquals(10, result.getMessage().length());
        assertTrue(result.getMessage().endsWith("..."));
    }

    @Test
    void disabledTruncatorReturnsOriginal() {
        TruncateConfig cfg = TruncateConfig.builder().maxMessageLength(5).enabled(false).build();
        LogTruncator truncator = new LogTruncator(cfg);
        LogEntry entry = entryWithMessage("this is longer than five chars");
        assertSame(entry, truncator.truncate(entry));
    }

    @Test
    void fieldTruncationWhenEnabled() {
        TruncateConfig cfg = TruncateConfig.builder()
                .maxFieldLength(8)
                .truncateFields(true)
                .truncationSuffix("~")
                .build();
        LogTruncator truncator = new LogTruncator(cfg);
        Map<String, String> fields = new HashMap<>();
        fields.put("key", "a_very_long_field_value");
        LogEntry entry = entryWithFields(fields);
        LogEntry result = truncator.truncate(entry);
        assertEquals(8, result.getFields().get("key").length());
        assertTrue(result.getFields().get("key").endsWith("~"));
    }

    @Test
    void fieldTruncationSkippedWhenDisabled() {
        TruncateConfig cfg = TruncateConfig.builder().maxFieldLength(5).truncateFields(false).build();
        LogTruncator truncator = new LogTruncator(cfg);
        Map<String, String> fields = new HashMap<>();
        fields.put("key", "a_very_long_value");
        LogEntry entry = entryWithFields(fields);
        LogEntry result = truncator.truncate(entry);
        // message is short, fields not truncated → same instance
        assertSame(entry, result);
    }

    @Test
    void requiresTruncationDetectsLongMessage() {
        TruncateConfig cfg = TruncateConfig.builder().maxMessageLength(5).build();
        LogTruncator truncator = new LogTruncator(cfg);
        assertTrue(truncator.requiresTruncation(entryWithMessage("toolong")));
        assertFalse(truncator.requiresTruncation(entryWithMessage("hi")));
    }

    @Test
    void nullEntryHandledGracefully() {
        LogTruncator truncator = new LogTruncator(TruncateConfig.builder().build());
        assertNull(truncator.truncate(null));
        assertFalse(truncator.requiresTruncation(null));
    }

    @Test
    void configBuilderValidatesPositiveLengths() {
        assertThrows(IllegalArgumentException.class,
                () -> TruncateConfig.builder().maxMessageLength(0).build());
        assertThrows(IllegalArgumentException.class,
                () -> TruncateConfig.builder().maxFieldLength(-1).build());
    }
}
