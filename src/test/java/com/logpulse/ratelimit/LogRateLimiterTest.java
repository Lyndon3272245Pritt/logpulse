package com.logpulse.ratelimit;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogRateLimiterTest {

    private LogRateLimiter rateLimiter;
    private RateLimiterConfig config;

    @BeforeEach
    void setUp() {
        config = RateLimiterConfig.builder()
                .maxEntriesPerWindow(3)
                .windowDurationMillis(5000L)
                .dropOnExceed(true)
                .build();
        rateLimiter = new LogRateLimiter(config);
    }

    private LogEntry entryForService(String service) {
        LogEntry entry = mock(LogEntry.class);
        when(entry.getService()).thenReturn(service);
        return entry;
    }

    @Test
    void firstEntryIsAlwaysAllowed() {
        assertTrue(rateLimiter.allow(entryForService("auth-service")));
    }

    @Test
    void nullEntryIsRejected() {
        assertFalse(rateLimiter.allow(null));
    }

    @Test
    void nullConfigThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LogRateLimiter(null));
    }

    @Test
    void differentServicesAreTrackedIndependently() {
        assertTrue(rateLimiter.allow(entryForService("service-a")));
        assertTrue(rateLimiter.allow(entryForService("service-b")));
        assertTrue(rateLimiter.allow(entryForService("service-a")));
        assertTrue(rateLimiter.allow(entryForService("service-b")));
    }

    @Test
    void entriesWithinLimitAreAllowed() {
        LogEntry e = entryForService("api");
        assertTrue(rateLimiter.allow(e));
        assertTrue(rateLimiter.allow(e));
        assertTrue(rateLimiter.allow(e));
    }

    @Test
    void resetClearsAllState() {
        LogEntry e = entryForService("svc");
        rateLimiter.allow(e);
        rateLimiter.allow(e);
        rateLimiter.reset();
        assertEquals(0, rateLimiter.getCurrentCount("svc"));
    }

    @Test
    void noDropModePassesAllEntries() {
        RateLimiterConfig noDropConfig = RateLimiterConfig.builder()
                .maxEntriesPerWindow(2)
                .windowDurationMillis(5000L)
                .dropOnExceed(false)
                .build();
        LogRateLimiter noDropLimiter = new LogRateLimiter(noDropConfig);
        LogEntry e = entryForService("svc");
        assertTrue(noDropLimiter.allow(e));
        assertTrue(noDropLimiter.allow(e));
        assertTrue(noDropLimiter.allow(e));
        assertTrue(noDropLimiter.allow(e));
    }

    @Test
    void unknownServiceReturnsZeroCount() {
        assertEquals(0, rateLimiter.getCurrentCount("unknown-service"));
    }
}
