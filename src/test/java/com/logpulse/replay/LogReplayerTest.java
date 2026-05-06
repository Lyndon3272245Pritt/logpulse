package com.logpulse.replay;

import com.logpulse.model.LogEntry;
import com.logpulse.parser.LogEntryParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogReplayerTest {

    @TempDir
    Path tempDir;

    private LogEntry makeEntry(String level, Instant ts) {
        LogEntry e = mock(LogEntry.class);
        when(e.getTimestamp()).thenReturn(ts);
        when(e.getLevel()).thenReturn(level);
        return e;
    }

    @Test
    void replayDeliversAllEntries() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        Files.writeString(logFile, "line1\nline2\nline3\n");

        Instant now = Instant.now();
        LogEntryParser parser = mock(LogEntryParser.class);
        when(parser.parse("line1")).thenReturn(makeEntry("INFO", now));
        when(parser.parse("line2")).thenReturn(makeEntry("WARN", now.plusMillis(10)));
        when(parser.parse("line3")).thenReturn(makeEntry("ERROR", now.plusMillis(20)));

        LogReplayConfig config = LogReplayConfig.builder(logFile.toString())
                .speedMultiplier(100.0)
                .build();

        List<LogEntry> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        LogReplayer replayer = new LogReplayer(config, parser);
        replayer.start(entry -> { received.add(entry); latch.countDown(); });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(3, received.size());
    }

    @Test
    void replayFiltersOutOfWindowEntries() throws Exception {
        Path logFile = tempDir.resolve("filter.log");
        Files.writeString(logFile, "old\nin\n");

        Instant boundary = Instant.parse("2024-06-01T00:00:00Z");
        LogEntryParser parser = mock(LogEntryParser.class);
        when(parser.parse("old")).thenReturn(makeEntry("INFO", boundary.minusSeconds(100)));
        when(parser.parse("in")).thenReturn(makeEntry("INFO", boundary.plusSeconds(10)));

        LogReplayConfig config = LogReplayConfig.builder(logFile.toString())
                .fromTime(boundary)
                .speedMultiplier(50.0)
                .build();

        List<LogEntry> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        LogReplayer replayer = new LogReplayer(config, parser);
        replayer.start(entry -> { received.add(entry); latch.countDown(); });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals("INFO", received.get(0).getLevel());
    }

    @Test
    void stopHaltsReplay() throws Exception {
        Path logFile = tempDir.resolve("big.log");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append("line").append(i).append("\n");
        Files.writeString(logFile, sb.toString());

        LogEntryParser parser = mock(LogEntryParser.class);
        when(parser.parse(anyString())).thenReturn(makeEntry("DEBUG", Instant.now()));

        LogReplayConfig config = LogReplayConfig.builder(logFile.toString())
                .speedMultiplier(0.001)
                .build();

        LogReplayer replayer = new LogReplayer(config, parser);
        replayer.start(entry -> {});
        assertTrue(replayer.isRunning());
        replayer.stop();

        Thread.sleep(200);
        assertFalse(replayer.isRunning());
    }

    @Test
    void doubleStartThrows() throws IOException {
        Path logFile = tempDir.resolve("dup.log");
        Files.writeString(logFile, "");
        LogEntryParser parser = mock(LogEntryParser.class);
        LogReplayConfig config = LogReplayConfig.builder(logFile.toString()).build();
        LogReplayer replayer = new LogReplayer(config, parser);
        replayer.start(e -> {});
        assertThrows(IllegalStateException.class, () -> replayer.start(e -> {}));
        replayer.stop();
    }
}
