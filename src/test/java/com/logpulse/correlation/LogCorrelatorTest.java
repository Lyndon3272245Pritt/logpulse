package com.logpulse.correlation;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogCorrelatorTest {

    private LogCorrelator correlator;
    private CorrelationConfig config;

    @BeforeEach
    void setUp() {
        config = CorrelationConfig.builder()
                .correlationField("correlationId")
                .windowSeconds(30)
                .build();
        correlator = new LogCorrelator(config);
    }

    private LogEntry entryWithCorrelation(String correlationId, String service) {
        Map<String, String> fields = new HashMap<>();
        fields.put("correlationId", correlationId);
        fields.put("service", service);
        return new LogEntry("INFO", "test message", service, System.currentTimeMillis(), fields);
    }

    @Test
    void constructorRejectsNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> new LogCorrelator(null));
    }

    @Test
    void addGroupsEntriesByCorrelationId() {
        correlator.add(entryWithCorrelation("abc-123", "auth-service"));
        correlator.add(entryWithCorrelation("abc-123", "order-service"));
        correlator.add(entryWithCorrelation("xyz-999", "payment-service"));

        assertEquals(2, correlator.getGroup("abc-123").size());
        assertEquals(1, correlator.getGroup("xyz-999").size());
        assertEquals(2, correlator.groupCount());
    }

    @Test
    void addIgnoresNullEntry() {
        assertDoesNotThrow(() -> correlator.add(null));
        assertEquals(0, correlator.groupCount());
    }

    @Test
    void addIgnoresEntryWithMissingCorrelationField() {
        LogEntry entry = new LogEntry("WARN", "no correlation", "svc", System.currentTimeMillis(), new HashMap<>());
        correlator.add(entry);
        assertEquals(0, correlator.groupCount());
    }

    @Test
    void getGroupReturnsEmptyListForUnknownId() {
        List<LogEntry> result = correlator.getGroup("nonexistent");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void clearRemovesAllGroups() {
        correlator.add(entryWithCorrelation("id-1", "svc-a"));
        correlator.add(entryWithCorrelation("id-2", "svc-b"));
        correlator.clear();
        assertEquals(0, correlator.groupCount());
    }

    @Test
    void evictExpiredRemovesOldGroups() throws InterruptedException {
        CorrelationConfig shortWindow = CorrelationConfig.builder()
                .correlationField("correlationId")
                .windowSeconds(1)
                .build();
        LogCorrelator shortCorrelator = new LogCorrelator(shortWindow);

        shortCorrelator.add(entryWithCorrelation("old-id", "svc"));
        assertEquals(1, shortCorrelator.groupCount());

        Thread.sleep(1100);
        int evicted = shortCorrelator.evictExpired();

        assertEquals(1, evicted);
        assertEquals(0, shortCorrelator.groupCount());
    }

    @Test
    void evictExpiredKeepsActiveGroups() {
        correlator.add(entryWithCorrelation("active-id", "svc"));
        int evicted = correlator.evictExpired();
        assertEquals(0, evicted);
        assertEquals(1, correlator.groupCount());
    }

    @Test
    void configDefaultsAreApplied() {
        CorrelationConfig defaults = CorrelationConfig.defaults();
        assertEquals("correlationId", defaults.getCorrelationField());
        assertEquals(60, defaults.getWindowSeconds());
    }

    @Test
    void configBuilderValidatesWindowBounds() {
        assertThrows(IllegalArgumentException.class, () ->
                CorrelationConfig.builder().windowSeconds(0).build());
        assertThrows(IllegalArgumentException.class, () ->
                CorrelationConfig.builder().windowSeconds(9999).build());
    }
}
