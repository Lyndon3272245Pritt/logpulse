package com.logpulse.tail;

import com.logpulse.model.LogEntry;
import com.logpulse.parser.LogEntryParser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class LogTailerTest {

    @TempDir
    Path tempDir;

    private LogEntryParser parser;

    @BeforeEach
    void setUp() {
        parser = new LogEntryParser();
    }

    @Test
    void constructor_throwsOnNullFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogTailer(null, parser, entry -> {}));
    }

    @Test
    void constructor_throwsOnNullParser() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogTailer(tempDir.resolve("x.log"), null, entry -> {}));
    }

    @Test
    void constructor_throwsOnNullConsumer() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogTailer(tempDir.resolve("x.log"), parser, null));
    }

    @Test
    void stop_setsRunningFalse() throws Exception {
        Path logFile = tempDir.resolve("app.log");
        Files.createFile(logFile);

        LogTailer tailer = new LogTailer(logFile, parser, entry -> {});
        Thread thread = new Thread(tailer);
        thread.start();

        // Give the tailer time to start
        Thread.sleep(300);
        assertTrue(tailer.isRunning(), "Tailer should be running");

        tailer.stop();
        thread.join(1000);

        assertFalse(tailer.isRunning(), "Tailer should have stopped");
    }

    @Test
    void run_parsesNewLinesAppendedToFile() throws Exception {
        Path logFile = tempDir.resolve("service.log");
        Files.createFile(logFile);

        List<LogEntry> received = new CopyOnWriteArrayList<>();
        LogTailer tailer = new LogTailer(logFile, parser, received::add);
        Thread thread = new Thread(tailer);
        thread.start();

        Thread.sleep(200);

        // Append a valid log line
        String validLine = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"level\":\"INFO\",\"service\":\"auth\",\"message\":\"User logged in\"}";
        try (BufferedWriter writer = Files.newBufferedWriter(logFile, StandardOpenOption.APPEND)) {
            writer.write(validLine);
            writer.newLine();
        }

        Thread.sleep(500);
        tailer.stop();
        thread.join(1000);

        assertFalse(received.isEmpty(), "Should have received at least one log entry");
    }
}
