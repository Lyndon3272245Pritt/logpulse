package com.logpulse.compress;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogCompressorTest {

    private CompressConfig enabledConfig;
    private CompressConfig disabledConfig;
    private LogCompressor compressor;

    @BeforeEach
    void setUp() {
        enabledConfig = new CompressConfig(true, 10);
        disabledConfig = new CompressConfig(false, 10);
        compressor = new LogCompressor(enabledConfig);
    }

    @Test
    void constructor_nullConfig_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new LogCompressor(null));
    }

    @Test
    void compress_nullEntry_returnsNull() {
        assertNull(compressor.compress(null));
    }

    @Test
    void compress_disabledConfig_returnsEntryUnchanged() {
        LogCompressor disabledCompressor = new LogCompressor(disabledConfig);
        LogEntry entry = makeEntry("This is a sufficiently long log message for testing.");
        LogEntry result = disabledCompressor.compress(entry);
        assertEquals(entry.getMessage(), result.getMessage());
    }

    @Test
    void compress_messageBelowThreshold_returnsEntryUnchanged() {
        LogEntry entry = makeEntry("short");
        LogEntry result = compressor.compress(entry);
        assertEquals("short", result.getMessage());
    }

    @Test
    void compress_messageMeetsThreshold_returnsCompressedEntry() {
        String longMessage = "A".repeat(200);
        LogEntry entry = makeEntry(longMessage);
        LogEntry result = compressor.compress(entry);
        assertNotEquals(longMessage, result.getMessage());
        assertNotNull(result.getMessage());
        // Compressed+Base64 encoded string should not equal original
        assertFalse(result.getMessage().isEmpty());
    }

    @Test
    void compress_preservesMetadata() {
        String longMessage = "B".repeat(100);
        LogEntry entry = makeEntry(longMessage);
        LogEntry result = compressor.compress(entry);
        assertEquals(entry.getService(), result.getService());
        assertEquals(entry.getLevel(), result.getLevel());
        assertEquals(entry.getTimestamp(), result.getTimestamp());
    }

    @Test
    void compressBatch_emptyList_returnsEmptyList() {
        List<LogEntry> result = compressor.compressBatch(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void compressBatch_nullList_returnsEmptyList() {
        List<LogEntry> result = compressor.compressBatch(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void compressBatch_multipleEntries_compressesEligible() {
        LogEntry small = makeEntry("tiny");
        LogEntry large = makeEntry("C".repeat(150));
        List<LogEntry> result = compressor.compressBatch(List.of(small, large));
        assertEquals(2, result.size());
        assertEquals("tiny", result.get(0).getMessage());
        assertNotEquals("C".repeat(150), result.get(1).getMessage());
    }

    @Test
    void estimateSavings_repetitiveContent_positiveOrZero() throws IOException {
        String repetitive = "ERROR ".repeat(100);
        long savings = compressor.estimateSavings(repetitive);
        assertTrue(savings >= 0);
    }

    @Test
    void estimateSavings_nullInput_returnsZero() throws IOException {
        assertEquals(0L, compressor.estimateSavings(null));
    }

    @Test
    void getConfig_returnsConfiguredInstance() {
        assertSame(enabledConfig, compressor.getConfig());
    }

    private LogEntry makeEntry(String message) {
        return new LogEntry(Instant.now(), "INFO", "test-service", message, Map.of());
    }
}
