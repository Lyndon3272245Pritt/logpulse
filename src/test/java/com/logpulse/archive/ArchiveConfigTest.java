package com.logpulse.archive;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveConfigTest {

    @Test
    void defaultsAreApplied() {
        ArchiveConfig config = ArchiveConfig.builder().build();
        assertEquals(Paths.get("archives"), config.getArchiveDirectory());
        assertEquals(Duration.ofDays(30), config.getRetentionPeriod());
        assertEquals(100 * 1024 * 1024L, config.getMaxArchiveSizeBytes());
        assertTrue(config.isCompressOnArchive());
        assertNotNull(config.getFileNamePattern());
    }

    @Test
    void customValuesAreApplied() {
        ArchiveConfig config = ArchiveConfig.builder()
                .archiveDirectory(Paths.get("/tmp/logs"))
                .retentionPeriod(Duration.ofDays(7))
                .maxArchiveSizeBytes(50 * 1024 * 1024L)
                .compressOnArchive(false)
                .fileNamePattern("{service}-{date}.archive")
                .build();

        assertEquals(Paths.get("/tmp/logs"), config.getArchiveDirectory());
        assertEquals(Duration.ofDays(7), config.getRetentionPeriod());
        assertEquals(50 * 1024 * 1024L, config.getMaxArchiveSizeBytes());
        assertFalse(config.isCompressOnArchive());
        assertEquals("{service}-{date}.archive", config.getFileNamePattern());
    }

    @Test
    void negativeRetentionThrows() {
        assertThrows(IllegalStateException.class, () ->
                ArchiveConfig.builder().retentionPeriod(Duration.ofDays(-1)).build());
    }

    @Test
    void zeroMaxSizeThrows() {
        assertThrows(IllegalStateException.class, () ->
                ArchiveConfig.builder().maxArchiveSizeBytes(0).build());
    }

    @Test
    void nullDirectoryThrows() {
        assertThrows(IllegalStateException.class, () ->
                ArchiveConfig.builder().archiveDirectory(null).build());
    }
}
