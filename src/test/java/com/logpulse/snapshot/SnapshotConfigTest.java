package com.logpulse.snapshot;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotConfigTest {

    @Test
    void defaultValuesAreApplied() {
        SnapshotConfig config = SnapshotConfig.builder().build();
        assertEquals(Paths.get("snapshots"), config.getOutputDirectory());
        assertEquals(Duration.ofMinutes(5), config.getInterval());
        assertEquals(10, config.getMaxSnapshots());
        assertFalse(config.isCompressOnWrite());
    }

    @Test
    void customValuesAreStored() {
        SnapshotConfig config = SnapshotConfig.builder()
                .outputDirectory(Paths.get("/tmp/logs"))
                .interval(Duration.ofSeconds(30))
                .maxSnapshots(5)
                .compressOnWrite(true)
                .build();

        assertEquals(Paths.get("/tmp/logs"), config.getOutputDirectory());
        assertEquals(Duration.ofSeconds(30), config.getInterval());
        assertEquals(5, config.getMaxSnapshots());
        assertTrue(config.isCompressOnWrite());
    }

    @Test
    void nullOutputDirectoryThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SnapshotConfig.builder().outputDirectory(null).build());
    }

    @Test
    void zeroIntervalThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SnapshotConfig.builder().interval(Duration.ZERO).build());
    }

    @Test
    void negativeIntervalThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SnapshotConfig.builder().interval(Duration.ofSeconds(-1)).build());
    }

    @Test
    void zeroMaxSnapshotsThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SnapshotConfig.builder().maxSnapshots(0).build());
    }
}
