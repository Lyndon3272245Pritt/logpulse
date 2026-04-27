package com.logpulse.formatter;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogFormatterTest {

    private LogFormatter plainFormatter;
    private LogFormatter colorFormatter;

    @BeforeEach
    void setUp() {
        plainFormatter = new LogFormatter(false);
        colorFormatter = new LogFormatter(true);
    }

    @Test
    void formatPlain_containsTimestampLevelServiceAndMessage() {
        LogEntry entry = buildEntry("INFO", "auth-service", "User logged in", null);
        String result = plainFormatter.format(entry);

        assertTrue(result.contains("2024-06-01 10:00:00"));
        assertTrue(result.contains("INFO"));
        assertTrue(result.contains("auth-service"));
        assertTrue(result.contains("User logged in"));
    }

    @Test
    void formatPlain_withExtraFields_appendsFieldsBlock() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("userId", "42");
        fields.put("ip", "127.0.0.1");
        LogEntry entry = buildEntry("DEBUG", "api-gateway", "Request received", fields);
        String result = plainFormatter.format(entry);

        assertTrue(result.contains("{userId=42 ip=127.0.0.1}"));
    }

    @Test
    void formatPlain_noFields_noFieldsBlock() {
        LogEntry entry = buildEntry("WARN", "db-service", "Slow query", null);
        String result = plainFormatter.format(entry);

        assertFalse(result.contains("{"));
    }

    @Test
    void formatColor_containsAnsiCodes() {
        LogEntry entry = buildEntry("ERROR", "payment-service", "Transaction failed", null);
        String result = colorFormatter.format(entry);

        assertTrue(result.contains("\u001B["));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    void formatPlain_nullTimestamp_usesUnknownTime() {
        LogEntry entry = new LogEntry();
        entry.setLevel("INFO");
        entry.setService("svc");
        entry.setMessage("msg");
        String result = plainFormatter.format(entry);

        assertTrue(result.contains("unknown-time"));
    }

    private LogEntry buildEntry(String level, String service, String message,
                                Map<String, String> fields) {
        LogEntry entry = new LogEntry();
        entry.setTimestamp(LocalDateTime.of(2024, 6, 1, 10, 0, 0));
        entry.setLevel(level);
        entry.setService(service);
        entry.setMessage(message);
        entry.setFields(fields);
        return entry;
    }
}
