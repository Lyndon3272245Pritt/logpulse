package com.logpulse.parser;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LogEntryParserTest {

    private LogEntryParser parser;

    @BeforeEach
    void setUp() {
        parser = new LogEntryParser();
    }

    @Test
    void parsesValidLogLine() {
        String line = "2024-01-15T10:23:45.123Z [auth-service] [INFO] User login successful";
        Optional<LogEntry> result = parser.parse(line);

        assertTrue(result.isPresent());
        LogEntry entry = result.get();
        assertEquals("auth-service", entry.getServiceName());
        assertEquals(LogEntry.Level.INFO, entry.getLevel());
        assertEquals("User login successful", entry.getMessage());
        assertEquals(line, entry.getRawLine());
    }

    @Test
    void parsesErrorLevel() {
        String line = "2024-01-15T10:23:45.000Z [payment-service] [ERROR] Payment gateway timeout";
        Optional<LogEntry> result = parser.parse(line);

        assertTrue(result.isPresent());
        assertEquals(LogEntry.Level.ERROR, result.get().getLevel());
        assertEquals("payment-service", result.get().getServiceName());
    }

    @Test
    void returnsEmptyForMalformedLine() {
        Optional<LogEntry> result = parser.parse("this is not a valid log line");
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForNullInput() {
        Optional<LogEntry> result = parser.parse(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForBlankInput() {
        Optional<LogEntry> result = parser.parse("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForInvalidLevel() {
        String line = "2024-01-15T10:23:45.000Z [svc] [VERBOSE] some message";
        Optional<LogEntry> result = parser.parse(line);
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForInvalidTimestamp() {
        String line = "not-a-timestamp [svc] [INFO] some message";
        Optional<LogEntry> result = parser.parse(line);
        assertTrue(result.isEmpty());
    }
}
