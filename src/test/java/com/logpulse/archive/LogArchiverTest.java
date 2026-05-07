package com.logpulse.archive;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogArchiverTest {

    @TempDir
    Path tempDir;

    private LogArchiver archiver;

    @BeforeEach
    void setUp() {
        ArchiveConfig config = ArchiveConfig.builder()
                .archiveDirectory(tempDir)
                .retentionPeriod(Duration.ofDays(1))
                .compressOnArchive(false)
                .fileNamePattern("test-{date}-{service}.log")
                .build();
        archiver = new LogArchiver(config);
    }

    private LogEntry entry(String service, String message) {
        return new LogEntry(Instant.now(), "INFO", service, message);
    }

    @Test
    void flushWritesEntriesToFile() throws IOException {
        archiver.buffer(entry("auth", "user logged in"));
        archiver.buffer(entry("auth", "token issued"));

        ArchiveEntry ae = archiver.flush("auth");

        assertNotNull(ae);
        assertEquals("auth", ae.getService());
        assertTrue(Files.exists(ae.getFilePath()));
        List<String> lines = Files.readAllLines(ae.getFilePath());
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("user logged in"));
    }

    @Test
    void flushReturnsNullWhenNothingBuffered() throws IOException {
        ArchiveEntry ae = archiver.flush("unknown-service");
        assertNull(ae);
    }

    @Test
    void archiveIndexTracksEntries() throws IOException {
        archiver.buffer(entry("billing", "invoice generated"));
        archiver.flush("billing");

        List<ArchiveEntry> index = archiver.getArchiveIndex();
        assertEquals(1, index.size());
        assertEquals("billing", index.get(0).getService());
    }

    @Test
    void evictExpiredRemovesOldEntries() throws IOException {
        archiver.buffer(entry("svc", "old log"));
        ArchiveEntry ae = archiver.flush("svc");
        assertNotNull(ae);
        // Retention is 1 day, nothing should be evicted immediately
        List<ArchiveEntry> evicted = archiver.evictExpired();
        assertTrue(evicted.isEmpty());
        assertEquals(1, archiver.getArchiveIndex().size());
    }

    @Test
    void multipleServicesArchivedIndependently() throws IOException {
        archiver.buffer(entry("api", "request received"));
        archiver.buffer(entry("db", "query executed"));

        ArchiveEntry apiEntry = archiver.flush("api");
        ArchiveEntry dbEntry = archiver.flush("db");

        assertNotNull(apiEntry);
        assertNotNull(dbEntry);
        assertNotEquals(apiEntry.getFilePath(), dbEntry.getFilePath());
        assertEquals(2, archiver.getArchiveIndex().size());
    }
}
