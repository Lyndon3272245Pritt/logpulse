package com.logpulse.filter;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogFilterTest {

    private LogEntry entry(String level, String service, String message) {
        return new LogEntry(Instant.now(), level, service, message);
    }

    @Test
    void noFilters_matchesEverything() {
        LogFilter filter = new LogFilter(null, null, null);
        assertTrue(filter.matches(entry("INFO", "auth-service", "User logged in")));
        assertTrue(filter.matches(entry("ERROR", "payment-service", "Payment failed")));
    }

    @Test
    void levelFilter_includesMatchingLevel() {
        LogFilter filter = new LogFilter(List.of("ERROR"), null, null);
        assertTrue(filter.matches(entry("ERROR", "api-gateway", "Timeout")));
        assertFalse(filter.matches(entry("INFO", "api-gateway", "Request received")));
    }

    @Test
    void levelFilter_isCaseInsensitive() {
        LogFilter filter = new LogFilter(List.of("WARN"), null, null);
        assertTrue(filter.matches(entry("warn", "svc", "Low memory")));
    }

    @Test
    void serviceFilter_includesMatchingService() {
        LogFilter filter = new LogFilter(null, List.of("auth-service"), null);
        assertTrue(filter.matches(entry("INFO", "auth-service", "Token issued")));
        assertFalse(filter.matches(entry("INFO", "order-service", "Order placed")));
    }

    @Test
    void messagePattern_matchesSubstring() {
        LogFilter filter = new LogFilter(null, null, "timeout");
        assertTrue(filter.matches(entry("ERROR", "db", "Connection timeout exceeded")));
        assertFalse(filter.matches(entry("ERROR", "db", "Disk full")));
    }

    @Test
    void messagePattern_isCaseInsensitive() {
        LogFilter filter = new LogFilter(null, null, "TIMEOUT");
        assertTrue(filter.matches(entry("ERROR", "db", "connection timeout")));
    }

    @Test
    void combinedFilters_allMustMatch() {
        LogFilter filter = new LogFilter(List.of("ERROR"), List.of("payment-service"), "failed");
        assertTrue(filter.matches(entry("ERROR", "payment-service", "Payment failed")));
        assertFalse(filter.matches(entry("INFO", "payment-service", "Payment failed")));
        assertFalse(filter.matches(entry("ERROR", "auth-service", "Payment failed")));
        assertFalse(filter.matches(entry("ERROR", "payment-service", "Payment succeeded")));
    }

    @Test
    void nullEntry_returnsFalse() {
        LogFilter filter = new LogFilter(null, null, null);
        assertFalse(filter.matches(null));
    }
}
