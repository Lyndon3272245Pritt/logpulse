package com.logpulse.watch;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class LogWatcherTest {

    @TempDir
    Path tempDir;

    private WatchConfig buildConfig(Duration interval) {
        return WatchConfig.builder()
            .watchPaths(List.of(tempDir.toString()))
            .includePatterns(List.of("*.log"))
            .pollInterval(interval)
            .recursive(false)
            .build();
    }

    @Test
    void detectsNewLogFile() throws Exception {
        List<Path> detected = new CopyOnWriteArrayList<>();
        WatchConfig config = buildConfig(Duration.ofMillis(100));

        try (LogWatcher watcher = new LogWatcher(config, detected::add)) {
            watcher.start();
            Path logFile = tempDir.resolve("app.log");
            Files.writeString(logFile, "first line\n");
            Thread.sleep(400);
        }

        assertFalse(detected.isEmpty(), "Should have detected app.log");
        assertTrue(detected.stream().anyMatch(p -> p.getFileName().toString().equals("app.log")));
    }

    @Test
    void ignoresExcludedPatterns() throws Exception {
        List<Path> detected = new CopyOnWriteArrayList<>();
        WatchConfig config = WatchConfig.builder()
            .watchPaths(List.of(tempDir.toString()))
            .includePatterns(List.of("*.log"))
            .excludePatterns(List.of("debug*.log"))
            .pollInterval(Duration.ofMillis(100))
            .build();

        try (LogWatcher watcher = new LogWatcher(config, detected::add)) {
            watcher.start();
            Files.writeString(tempDir.resolve("debug-verbose.log"), "noise");
            Files.writeString(tempDir.resolve("error.log"), "important");
            Thread.sleep(400);
        }

        assertTrue(detected.stream().noneMatch(p -> p.getFileName().toString().startsWith("debug")),
            "debug*.log should be excluded");
        assertTrue(detected.stream().anyMatch(p -> p.getFileName().toString().equals("error.log")),
            "error.log should be detected");
    }

    @Test
    void detectsModifiedFile() throws Exception {
        Path logFile = tempDir.resolve("service.log");
        Files.writeString(logFile, "initial\n");

        List<Path> detected = new CopyOnWriteArrayList<>();
        WatchConfig config = buildConfig(Duration.ofMillis(100));

        try (LogWatcher watcher = new LogWatcher(config, detected::add)) {
            watcher.start();
            Thread.sleep(250);
            int countAfterFirst = detected.size();
            Thread.sleep(150);
            Files.writeString(logFile, "updated\n", StandardOpenOption.APPEND);
            // ensure last-modified advances
            logFile.toFile().setLastModified(System.currentTimeMillis() + 1000);
            Thread.sleep(400);
            assertTrue(detected.size() > countAfterFirst, "Should detect modification");
        }
    }

    @Test
    void watchConfigRequiresAtLeastOnePath() {
        assertThrows(IllegalStateException.class, () ->
            WatchConfig.builder()
                .watchPaths(Collections.emptyList())
                .build());
    }

    @Test
    void watchConfigRejectsBadPollInterval() {
        assertThrows(IllegalArgumentException.class, () ->
            WatchConfig.builder()
                .watchPaths(List.of(tempDir.toString()))
                .pollInterval(Duration.ZERO)
                .build());
    }
}
