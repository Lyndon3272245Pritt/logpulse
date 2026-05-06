package com.logpulse.checkpoint;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointManagerTest {

    @TempDir
    Path tempDir;

    private CheckpointConfig config;

    @BeforeEach
    void setUp() {
        config = CheckpointConfig.builder()
                .checkpointFilePath(tempDir.resolve("checkpoints.properties").toString())
                .build();
    }

    @Test
    void returnsZeroForUnknownSource() throws IOException {
        CheckpointManager manager = new CheckpointManager(config);
        assertEquals(0L, manager.getOffset("/var/log/app.log"));
    }

    @Test
    void savesAndRetrievesOffset() throws IOException {
        CheckpointManager manager = new CheckpointManager(config);
        manager.saveOffset("/var/log/app.log", 1024L);
        assertEquals(1024L, manager.getOffset("/var/log/app.log"));
    }

    @Test
    void persistsAcrossInstances() throws IOException {
        CheckpointManager first = new CheckpointManager(config);
        first.saveOffset("/var/log/svc-a.log", 2048L);
        first.saveOffset("/var/log/svc-b.log", 512L);

        CheckpointManager second = new CheckpointManager(config);
        assertEquals(2048L, second.getOffset("/var/log/svc-a.log"));
        assertEquals(512L, second.getOffset("/var/log/svc-b.log"));
    }

    @Test
    void clearOffsetRemovesEntry() throws IOException {
        CheckpointManager manager = new CheckpointManager(config);
        manager.saveOffset("/var/log/app.log", 999L);
        manager.clearOffset("/var/log/app.log");
        assertEquals(0L, manager.getOffset("/var/log/app.log"));
    }

    @Test
    void clearOffsetIsPersisted() throws IOException {
        CheckpointManager first = new CheckpointManager(config);
        first.saveOffset("/var/log/app.log", 777L);
        first.clearOffset("/var/log/app.log");

        CheckpointManager second = new CheckpointManager(config);
        assertEquals(0L, second.getOffset("/var/log/app.log"));
    }

    @Test
    void getAllOffsetsReturnsSnapshot() throws IOException {
        CheckpointManager manager = new CheckpointManager(config);
        manager.saveOffset("/a", 10L);
        manager.saveOffset("/b", 20L);

        Map<String, Long> all = manager.getAllOffsets();
        assertEquals(2, all.size());
        assertEquals(10L, all.get("/a"));
        assertEquals(20L, all.get("/b"));
    }

    @Test
    void overwritingOffsetUpdatesValue() throws IOException {
        CheckpointManager manager = new CheckpointManager(config);
        manager.saveOffset("/var/log/app.log", 100L);
        manager.saveOffset("/var/log/app.log", 200L);
        assertEquals(200L, manager.getOffset("/var/log/app.log"));
    }
}
