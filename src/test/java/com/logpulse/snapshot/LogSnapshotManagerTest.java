package com.logpulse.snapshot;

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

class LogSnapshotManagerTest {

    @TempDir
    Path tempDir;

    private LogSnapshotManager manager;

    @BeforeEach
    void setUp() {
        SnapshotConfig config = SnapshotConfig.builder()
                .outputDirectory(tempDir)
                .interval(Duration.ofMinutes(1))
                .maxSnapshots(3)
                .build();
        manager = new LogSnapshotManager(config);
    }

    private LogEntry entry(String raw) {
        return new LogEntry("svc", "INFO", raw, Instant.now());
    }

    @Test
    void snapshotFileIsCreated() throws IOException {
        List<LogEntry> entries = List.of(entry("line one"), entry("line two"));
        Path snapshot = manager.takeSnapshot(entries);

        assertTrue(Files.exists(snapshot));
        List<String> lines = Files.readAllLines(snapshot);
        assertEquals(2, lines.size());
        assertEquals("line one", lines.get(0));
        assertEquals("line two", lines.get(1));
    }

    @Test
    void snapshotHistoryTracksFiles() throws IOException {
        manager.takeSnapshot(List.of(entry("a")));
        manager.takeSnapshot(List.of(entry("b")));

        assertEquals(2, manager.getSnapshotHistory().size());
    }

    @Test
    void oldSnapshotsPrunedWhenMaxExceeded() throws IOException {
        Path first = manager.takeSnapshot(List.of(entry("1")));
        manager.takeSnapshot(List.of(entry("2")));
        manager.takeSnapshot(List.of(entry("3")));
        // This fourth snapshot should trigger pruning of the first
        manager.takeSnapshot(List.of(entry("4")));

        List<Path> history = manager.getSnapshotHistory();
        assertEquals(3, history.size());
        assertFalse(Files.exists(first), "oldest snapshot should have been deleted");
    }

    @Test
    void emptyEntriesProducesEmptyFile() throws IOException {
        Path snapshot = manager.takeSnapshot(List.of());
        assertTrue(Files.exists(snapshot));
        assertEquals(0, Files.size(snapshot));
    }

    @Test
    void nullConfigThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LogSnapshotManager(null));
    }
}
