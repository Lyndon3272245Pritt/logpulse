package com.logpulse.deduplicate;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicateConfigTest {

    @Test
    void shouldBuildWithDefaults() {
        DeduplicateConfig config = DeduplicateConfig.builder().build();

        assertEquals(Duration.ofSeconds(30), config.getWindowDuration());
        assertEquals(1000, config.getMaxCacheSize());
        assertTrue(config.isCaseSensitive());
        assertTrue(config.isIncludeServiceInKey());
    }

    @Test
    void shouldBuildWithCustomValues() {
        DeduplicateConfig config = DeduplicateConfig.builder()
                .windowDuration(Duration.ofMinutes(5))
                .maxCacheSize(500)
                .caseSensitive(false)
                .includeServiceInKey(false)
                .build();

        assertEquals(Duration.ofMinutes(5), config.getWindowDuration());
        assertEquals(500, config.getMaxCacheSize());
        assertFalse(config.isCaseSensitive());
        assertFalse(config.isIncludeServiceInKey());
    }

    @Test
    void shouldRejectNullWindowDuration() {
        assertThrows(NullPointerException.class, () ->
                DeduplicateConfig.builder().windowDuration(null).build());
    }

    @Test
    void shouldRejectZeroWindowDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                DeduplicateConfig.builder().windowDuration(Duration.ZERO).build());
    }

    @Test
    void shouldRejectNegativeWindowDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                DeduplicateConfig.builder().windowDuration(Duration.ofSeconds(-1)).build());
    }

    @Test
    void shouldRejectZeroMaxCacheSize() {
        assertThrows(IllegalArgumentException.class, () ->
                DeduplicateConfig.builder().maxCacheSize(0).build());
    }

    @Test
    void shouldRejectNegativeMaxCacheSize() {
        assertThrows(IllegalArgumentException.class, () ->
                DeduplicateConfig.builder().maxCacheSize(-10).build());
    }

    @Test
    void shouldAcceptMinimalValidConfig() {
        DeduplicateConfig config = DeduplicateConfig.builder()
                .windowDuration(Duration.ofMillis(1))
                .maxCacheSize(1)
                .build();

        assertEquals(Duration.ofMillis(1), config.getWindowDuration());
        assertEquals(1, config.getMaxCacheSize());
    }
}
