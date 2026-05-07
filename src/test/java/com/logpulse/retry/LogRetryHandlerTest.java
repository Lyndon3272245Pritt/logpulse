package com.logpulse.retry;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LogRetryHandlerTest {

    private LogEntry entry;

    @BeforeEach
    void setUp() {
        entry = new LogEntry("auth-service", "ERROR", "Connection refused", Instant.now());
    }

    private RetryConfig fastConfig() {
        return RetryConfig.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofMillis(1))
                .backoffMultiplier(1.0)
                .maxDelay(Duration.ofMillis(5))
                .build();
    }

    @Test
    void successOnFirstAttempt() {
        LogRetryHandler handler = new LogRetryHandler(fastConfig());
        boolean result = handler.attempt(entry, e -> { /* no-op success */ });
        assertTrue(result);
        assertEquals(1, handler.getTotalAttempts());
        assertEquals(1, handler.getTotalSuccesses());
        assertEquals(0, handler.getTotalFailures());
    }

    @Test
    void retriesAndSucceedsOnSecondAttempt() {
        LogRetryHandler handler = new LogRetryHandler(fastConfig());
        AtomicInteger callCount = new AtomicInteger(0);
        boolean result = handler.attempt(entry, e -> {
            if (callCount.incrementAndGet() < 2) throw new RuntimeException("transient");
        });
        assertTrue(result);
        assertEquals(2, handler.getTotalAttempts());
        assertEquals(1, handler.getTotalSuccesses());
    }

    @Test
    void exhaustsAllAttemptsAndReturnsFalse() {
        LogRetryHandler handler = new LogRetryHandler(fastConfig());
        boolean result = handler.attempt(entry, e -> { throw new RuntimeException("always fails"); });
        assertFalse(result);
        assertEquals(3, handler.getTotalAttempts());
        assertEquals(0, handler.getTotalSuccesses());
        assertEquals(1, handler.getTotalFailures());
    }

    @Test
    void resetStatsClearsCounters() {
        LogRetryHandler handler = new LogRetryHandler(fastConfig());
        handler.attempt(entry, e -> { throw new RuntimeException("fail"); });
        handler.resetStats();
        assertEquals(0, handler.getTotalAttempts());
        assertEquals(0, handler.getTotalSuccesses());
        assertEquals(0, handler.getTotalFailures());
    }

    @Test
    void nullConfigThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LogRetryHandler(null));
    }
}
