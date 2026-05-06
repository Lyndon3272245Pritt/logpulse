package com.logpulse.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterConfigTest {

    @Test
    void defaultValuesAreApplied() {
        RateLimiterConfig config = RateLimiterConfig.builder().build();
        assertEquals(100, config.getMaxEntriesPerWindow());
        assertEquals(1000L, config.getWindowDurationMillis());
        assertTrue(config.isDropOnExceed());
    }

    @Test
    void customValuesAreApplied() {
        RateLimiterConfig config = RateLimiterConfig.builder()
                .maxEntriesPerWindow(50)
                .windowDurationMillis(2000L)
                .dropOnExceed(false)
                .build();
        assertEquals(50, config.getMaxEntriesPerWindow());
        assertEquals(2000L, config.getWindowDurationMillis());
        assertFalse(config.isDropOnExceed());
    }

    @Test
    void negativeMaxEntriesThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                RateLimiterConfig.builder().maxEntriesPerWindow(-1).build());
    }

    @Test
    void zeroMaxEntriesThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                RateLimiterConfig.builder().maxEntriesPerWindow(0).build());
    }

    @Test
    void negativeWindowDurationThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                RateLimiterConfig.builder().windowDurationMillis(-500L).build());
    }

    @Test
    void builderIsReusable() {
        RateLimiterConfig.Builder builder = RateLimiterConfig.builder().maxEntriesPerWindow(10);
        RateLimiterConfig c1 = builder.build();
        RateLimiterConfig c2 = builder.windowDurationMillis(3000L).build();
        assertEquals(10, c1.getMaxEntriesPerWindow());
        assertEquals(10, c2.getMaxEntriesPerWindow());
        assertEquals(3000L, c2.getWindowDurationMillis());
    }
}
