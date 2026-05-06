package com.logpulse.throttle;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LogThrottlerTest {

    private ThrottleConfig config;
    private LogThrottler throttler;

    @BeforeEach
    void setUp() {
        config = ThrottleConfig.builder()
                .enabled(true)
                .maxEntriesPerWindow(3)
                .windowMillis(5000)
                .build();
        throttler = new LogThrottler(config);
    }

    @Test
    void allowsEntriesWithinLimit() {
        LogEntry entry = buildEntry("auth-service", "ERROR");
        assertTrue(throttler.allow(entry));
        assertTrue(throttler.allow(entry));
        assertTrue(throttler.allow(entry));
    }

    @Test
    void suppressesEntriesExceedingLimit() {
        LogEntry entry = buildEntry("auth-service", "ERROR");
        throttler.allow(entry);
        throttler.allow(entry);
        throttler.allow(entry);
        assertFalse(throttler.allow(entry), "4th entry should be throttled");
    }

    @Test
    void tracksKeysPerServiceAndLevel() {
        throttler.allow(buildEntry("auth-service", "ERROR"));
        throttler.allow(buildEntry("payment-service", "WARN"));
        throttler.allow(buildEntry("auth-service", "INFO"));
        assertEquals(3, throttler.trackedKeyCount());
    }

    @Test
    void differentServicesHaveIndependentLimits() {
        LogEntry authEntry = buildEntry("auth-service", "ERROR");
        LogEntry payEntry = buildEntry("payment-service", "ERROR");
        for (int i = 0; i < 3; i++) {
            assertTrue(throttler.allow(authEntry));
        }
        assertFalse(throttler.allow(authEntry));
        assertTrue(throttler.allow(payEntry), "Different service should still be allowed");
    }

    @Test
    void allowsAllWhenDisabled() {
        ThrottleConfig disabledConfig = ThrottleConfig.builder()
                .enabled(false)
                .maxEntriesPerWindow(1)
                .windowMillis(5000)
                .build();
        LogThrottler disabledThrottler = new LogThrottler(disabledConfig);
        LogEntry entry = buildEntry("svc", "DEBUG");
        for (int i = 0; i < 10; i++) {
            assertTrue(disabledThrottler.allow(entry));
        }
    }

    @Test
    void resetClearsAllCounters() {
        LogEntry entry = buildEntry("svc", "ERROR");
        throttler.allow(entry);
        throttler.allow(entry);
        throttler.allow(entry);
        assertFalse(throttler.allow(entry));
        throttler.reset();
        assertTrue(throttler.allow(entry), "Should be allowed after reset");
        assertEquals(1, throttler.trackedKeyCount());
    }

    @Test
    void rejectsNullEntry() {
        assertFalse(throttler.allow(null));
    }

    @Test
    void constructorRejectsNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> new LogThrottler(null));
    }

    private LogEntry buildEntry(String service, String level) {
        return LogEntry.builder()
                .service(service)
                .level(level)
                .message("test message")
                .timestamp(Instant.now())
                .build();
    }
}
