package com.logpulse.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RetryConfigTest {

    @Test
    void defaultValuesAreApplied() {
        RetryConfig config = RetryConfig.builder().build();
        assertEquals(3, config.getMaxAttempts());
        assertEquals(Duration.ofMillis(200), config.getInitialDelay());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(10), config.getMaxDelay());
        assertFalse(config.isRetryOnAllFailures());
    }

    @Test
    void customValuesAreApplied() {
        RetryConfig config = RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofMillis(100))
                .backoffMultiplier(1.5)
                .maxDelay(Duration.ofSeconds(5))
                .retryOnAllFailures(true)
                .build();
        assertEquals(5, config.getMaxAttempts());
        assertEquals(Duration.ofMillis(100), config.getInitialDelay());
        assertEquals(1.5, config.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(5), config.getMaxDelay());
        assertTrue(config.isRetryOnAllFailures());
    }

    @Test
    void delayForAttemptAppliesExponentialBackoff() {
        RetryConfig config = RetryConfig.builder()
                .initialDelay(Duration.ofMillis(100))
                .backoffMultiplier(2.0)
                .maxDelay(Duration.ofSeconds(10))
                .build();
        assertEquals(100, config.delayForAttempt(1).toMillis());
        assertEquals(200, config.delayForAttempt(2).toMillis());
        assertEquals(400, config.delayForAttempt(3).toMillis());
    }

    @Test
    void delayIsCappedAtMaxDelay() {
        RetryConfig config = RetryConfig.builder()
                .initialDelay(Duration.ofMillis(500))
                .backoffMultiplier(10.0)
                .maxDelay(Duration.ofMillis(1000))
                .build();
        assertTrue(config.delayForAttempt(3).toMillis() <= 1000);
    }

    @Test
    void invalidMaxAttemptsThrows() {
        assertThrows(IllegalArgumentException.class, () -> RetryConfig.builder().maxAttempts(0).build());
    }
}
