package com.logpulse.normalize;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogNormalizerTest {

    private NormalizeConfig config;

    @BeforeEach
    void setUp() {
        config = NormalizeConfig.builder()
                .renameField("lvl", "level_normalized")
                .defaultField("env", "production")
                .lowercaseLevel(true)
                .trimWhitespace(true)
                .build();
    }

    private LogEntry entry(String service, String level, String message, Map<String, String> fields) {
        return new LogEntry(Instant.now(), service, level, message, fields);
    }

    @Test
    void nullEntryReturnsNull() {
        LogNormalizer normalizer = new LogNormalizer(config);
        assertNull(normalizer.normalize(null));
    }

    @Test
    void levelIsLowercased() {
        LogNormalizer normalizer = new LogNormalizer(config);
        LogEntry result = normalizer.normalize(entry("svc", "WARN", "msg", new HashMap<>()));
        assertEquals("warn", result.getLevel());
    }

    @Test
    void levelNotLowercasedWhenDisabled() {
        NormalizeConfig noLower = NormalizeConfig.builder().lowercaseLevel(false).build();
        LogNormalizer normalizer = new LogNormalizer(noLower);
        LogEntry result = normalizer.normalize(entry("svc", "ERROR", "msg", new HashMap<>()));
        assertEquals("ERROR", result.getLevel());
    }

    @Test
    void whitespaceIsTrimmed() {
        Map<String, String> fields = new HashMap<>();
        fields.put("host", "  server1  ");
        LogNormalizer normalizer = new LogNormalizer(config);
        LogEntry result = normalizer.normalize(entry("  my-svc  ", "info", "  hello  ", fields));
        assertEquals("my-svc", result.getService());
        assertEquals("hello", result.getMessage());
        assertEquals("server1", result.getFields().get("host"));
    }

    @Test
    void fieldRenameApplied() {
        Map<String, String> fields = new HashMap<>();
        fields.put("lvl", "debug");
        LogNormalizer normalizer = new LogNormalizer(config);
        LogEntry result = normalizer.normalize(entry("svc", "info", "msg", fields));
        assertFalse(result.getFields().containsKey("lvl"), "Original key should be removed");
        assertTrue(result.getFields().containsKey("level_normalized"), "Renamed key should be present");
        assertEquals("debug", result.getFields().get("level_normalized"));
    }

    @Test
    void defaultFieldInjectedWhenAbsent() {
        LogNormalizer normalizer = new LogNormalizer(config);
        LogEntry result = normalizer.normalize(entry("svc", "info", "msg", new HashMap<>()));
        assertEquals("production", result.getFields().get("env"));
    }

    @Test
    void defaultFieldInjectedWhenBlank() {
        Map<String, String> fields = new HashMap<>();
        fields.put("env", "   ");
        LogNormalizer normalizer = new LogNormalizer(config);
        LogEntry result = normalizer.normalize(entry("svc", "info", "msg", fields));
        assertEquals("production", result.getFields().get("env"));
    }

    @Test
    void existingDefaultFieldNotOverwritten() {
        Map<String, String> fields = new HashMap<>();
        fields.put("env", "staging");
        LogNormalizer normalizer = new LogNormalizer(config);
        LogEntry result = normalizer.normalize(entry("svc", "info", "msg", fields));
        assertEquals("staging", result.getFields().get("env"));
    }

    @Test
    void nullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new LogNormalizer(null));
    }
}
