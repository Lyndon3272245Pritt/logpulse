package com.logpulse.metrics;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogMetricsCollectorTest {

    private LogMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new LogMetricsCollector();
    }

    private LogEntry entry(String service, String level) {
        return new LogEntry(service, level, "Test message", Instant.now());
    }

    @Test
    void testInitialCountsAreZero() {
        assertEquals(0, collector.getTotalProcessed());
        assertEquals(0, collector.getTotalDropped());
        assertEquals(0, collector.getTotalFiltered());
    }

    @Test
    void testRecordProcessedIncrementsTotals() {
        collector.recordProcessed(entry("auth-service", "INFO"));
        collector.recordProcessed(entry("auth-service", "ERROR"));
        collector.recordProcessed(entry("payment-service", "WARN"));

        assertEquals(3, collector.getTotalProcessed());
    }

    @Test
    void testCountByServiceIsAccurate() {
        collector.recordProcessed(entry("auth-service", "INFO"));
        collector.recordProcessed(entry("auth-service", "INFO"));
        collector.recordProcessed(entry("payment-service", "ERROR"));

        Map<String, Long> byService = collector.getCountByService();
        assertEquals(2L, byService.get("auth-service"));
        assertEquals(1L, byService.get("payment-service"));
    }

    @Test
    void testCountByLevelIsAccurate() {
        collector.recordProcessed(entry("svc", "INFO"));
        collector.recordProcessed(entry("svc", "INFO"));
        collector.recordProcessed(entry("svc", "ERROR"));

        Map<String, Long> byLevel = collector.getCountByLevel();
        assertEquals(2L, byLevel.get("INFO"));
        assertEquals(1L, byLevel.get("ERROR"));
    }

    @Test
    void testRecordDroppedAndFiltered() {
        collector.recordDropped();
        collector.recordDropped();
        collector.recordFiltered();

        assertEquals(2, collector.getTotalDropped());
        assertEquals(1, collector.getTotalFiltered());
    }

    @Test
    void testRecordProcessedWithNullDoesNotThrow() {
        assertDoesNotThrow(() -> collector.recordProcessed(null));
        assertEquals(0, collector.getTotalProcessed());
    }

    @Test
    void testReset() {
        collector.recordProcessed(entry("svc", "INFO"));
        collector.recordDropped();
        collector.recordFiltered();

        collector.reset();

        assertEquals(0, collector.getTotalProcessed());
        assertEquals(0, collector.getTotalDropped());
        assertEquals(0, collector.getTotalFiltered());
        assertTrue(collector.getCountByService().isEmpty());
        assertTrue(collector.getCountByLevel().isEmpty());
    }

    @Test
    void testThroughputIsNonNegative() {
        collector.recordProcessed(entry("svc", "INFO"));
        assertTrue(collector.getThroughputPerSecond() >= 0);
    }
}
