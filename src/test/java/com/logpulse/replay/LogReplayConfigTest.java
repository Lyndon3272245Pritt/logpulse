package com.logpulse.replay;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LogReplayConfigTest {

    @Test
    void buildWithDefaults() {
        LogReplayConfig config = LogReplayConfig.builder("/var/log/app.log").build();
        assertEquals("/var/log/app.log", config.getSourcePath());
        assertEquals(1.0, config.getSpeedMultiplier());
        assertFalse(config.isLoop());
        assertEquals(Integer.MAX_VALUE, config.getMaxEntriesPerSecond());
        assertEquals(Instant.EPOCH, config.getFromTime());
        assertEquals(Instant.MAX, config.getToTime());
    }

    @Test
    void buildWithCustomValues() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-02T00:00:00Z");

        LogReplayConfig config = LogReplayConfig.builder("/logs/svc.log")
                .fromTime(from)
                .toTime(to)
                .speedMultiplier(2.5)
                .loop(true)
                .maxEntriesPerSecond(500)
                .build();

        assertEquals("/logs/svc.log", config.getSourcePath());
        assertEquals(from, config.getFromTime());
        assertEquals(to, config.getToTime());
        assertEquals(2.5, config.getSpeedMultiplier());
        assertTrue(config.isLoop());
        assertEquals(500, config.getMaxEntriesPerSecond());
    }

    @Test
    void blankSourcePathThrows() {
        assertThrows(IllegalArgumentException.class, () -> LogReplayConfig.builder("").build());
        assertThrows(IllegalArgumentException.class, () -> LogReplayConfig.builder("   ").build());
        assertThrows(IllegalArgumentException.class, () -> LogReplayConfig.builder(null).build());
    }

    @Test
    void invalidSpeedMultiplierThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> LogReplayConfig.builder("/log").speedMultiplier(0).build());
        assertThrows(IllegalArgumentException.class,
                () -> LogReplayConfig.builder("/log").speedMultiplier(-1.5).build());
    }

    @Test
    void invalidMaxEntriesThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> LogReplayConfig.builder("/log").maxEntriesPerSecond(0).build());
        assertThrows(IllegalArgumentException.class,
                () -> LogReplayConfig.builder("/log").maxEntriesPerSecond(-100).build());
    }
}
