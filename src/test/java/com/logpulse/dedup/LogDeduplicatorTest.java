package com.logpulse.dedup;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LogDeduplicatorTest {

    private LogDeduplicator deduplicator;

    @BeforeEach
    void setUp() {
        // 5-second window, cache up to 100 entries
        deduplicator = new LogDeduplicator(5_000, 100);
    }

    private LogEntry entry(String service, String level, String message, Instant ts) {
        return new LogEntry(service, level, message, ts, null);
    }

    @Test
    void firstOccurrenceIsNotDuplicate() {
        LogEntry e = entry("auth", "ERROR", "connection refused", Instant.now());
        assertFalse(deduplicator.isDuplicate(e));
    }

    @Test
    void sameEntryWithinWindowIsDuplicate() {
        Instant base = Instant.ofEpochMilli(1_000_000);
        LogEntry first  = entry("auth", "ERROR", "connection refused", base);
        LogEntry second = entry("auth", "ERROR", "connection refused", base.plusMillis(2_000));
        assertFalse(deduplicator.isDuplicate(first));
        assertTrue(deduplicator.isDuplicate(second));
    }

    @Test
    void sameEntryAfterWindowIsNotDuplicate() {
        Instant base = Instant.ofEpochMilli(1_000_000);
        LogEntry first  = entry("auth", "ERROR", "connection refused", base);
        LogEntry later  = entry("auth", "ERROR", "connection refused", base.plusMillis(6_000));
        assertFalse(deduplicator.isDuplicate(first));
        assertFalse(deduplicator.isDuplicate(later));
    }

    @Test
    void differentServicesAreNotDuplicates() {
        Instant now = Instant.now();
        LogEntry a = entry("auth",    "WARN", "slow query", now);
        LogEntry b = entry("billing", "WARN", "slow query", now.plusMillis(100));
        assertFalse(deduplicator.isDuplicate(a));
        assertFalse(deduplicator.isDuplicate(b));
    }

    @Test
    void differentLevelsAreNotDuplicates() {
        Instant now = Instant.now();
        assertFalse(deduplicator.isDuplicate(entry("svc", "WARN",  "msg", now)));
        assertFalse(deduplicator.isDuplicate(entry("svc", "ERROR", "msg", now.plusMillis(100))));
    }

    @Test
    void resetClearsCache() {
        Instant base = Instant.ofEpochMilli(2_000_000);
        LogEntry e = entry("svc", "INFO", "started", base);
        assertFalse(deduplicator.isDuplicate(e));
        deduplicator.reset();
        assertEquals(0, deduplicator.getCacheSize());
        assertFalse(deduplicator.isDuplicate(entry("svc", "INFO", "started", base.plusMillis(100))));
    }

    @Test
    void nullEntryThrows() {
        assertThrows(NullPointerException.class, () -> deduplicator.isDuplicate(null));
    }

    @Test
    void invalidWindowThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LogDeduplicator(-1, 10));
    }

    @Test
    void invalidCacheSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LogDeduplicator(1000, 0));
    }
}
