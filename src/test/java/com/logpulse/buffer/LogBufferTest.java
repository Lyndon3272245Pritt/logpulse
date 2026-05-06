package com.logpulse.buffer;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogBufferTest {

    private LogBuffer buffer;

    private static LogEntry entry(String message) {
        return new LogEntry("test-service", "INFO", message, Instant.now());
    }

    @BeforeEach
    void setUp() {
        buffer = new LogBuffer(BufferConfig.builder().capacity(4).drainTimeoutMs(50).build());
    }

    @Test
    void offerAndSizeTracked() {
        buffer.offer(entry("a"));
        buffer.offer(entry("b"));
        assertEquals(2, buffer.size());
        assertFalse(buffer.isEmpty());
    }

    @Test
    void offerNullReturnsFalse() {
        assertFalse(buffer.offer(null));
        assertEquals(0, buffer.size());
    }

    @Test
    void overflowDropsOldestAndSetsFlag() {
        for (int i = 0; i < 5; i++) {
            buffer.offer(entry("msg-" + i));
        }
        assertTrue(buffer.isOverflow());
        assertEquals(4, buffer.size());
        List<LogEntry> drained = buffer.drainAll();
        // oldest (msg-0) should have been dropped; msg-1 through msg-4 remain
        assertEquals("msg-1", drained.get(0).getMessage());
    }

    @Test
    void resetOverflowClearsFlag() {
        for (int i = 0; i < 5; i++) {
            buffer.offer(entry("x"));
        }
        assertTrue(buffer.isOverflow());
        buffer.resetOverflow();
        assertFalse(buffer.isOverflow());
    }

    @Test
    void drainAllReturnsAllEntries() {
        buffer.offer(entry("one"));
        buffer.offer(entry("two"));
        List<LogEntry> result = buffer.drainAll();
        assertEquals(2, result.size());
        assertTrue(buffer.isEmpty());
    }

    @Test
    void drainWithTimeoutReturnsEntriesInBatch() throws InterruptedException {
        buffer.offer(entry("a"));
        buffer.offer(entry("b"));
        buffer.offer(entry("c"));
        List<LogEntry> target = new ArrayList<>();
        int count = buffer.drain(target, 2, 50);
        assertEquals(2, count);
        assertEquals(2, target.size());
    }

    @Test
    void drainTimesOutOnEmptyBuffer() throws InterruptedException {
        List<LogEntry> target = new ArrayList<>();
        int count = buffer.drain(target, 10, 30);
        assertEquals(0, count);
        assertTrue(target.isEmpty());
    }

    @Test
    void defaultConfigHasExpectedValues() {
        BufferConfig cfg = BufferConfig.defaults();
        assertEquals(BufferConfig.DEFAULT_CAPACITY, cfg.getCapacity());
        assertEquals(BufferConfig.DEFAULT_DRAIN_TIMEOUT_MS, cfg.getDrainTimeoutMs());
    }

    @Test
    void builderRejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> BufferConfig.builder().capacity(0).build());
    }
}
