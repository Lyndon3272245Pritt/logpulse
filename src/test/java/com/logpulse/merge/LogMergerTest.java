package com.logpulse.merge;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogMergerTest {

    private LogMerger merger;

    private LogEntry entryAt(String source, long epochMilli, String message) {
        LogEntry e = new LogEntry();
        e.setSource(source);
        e.setTimestamp(Instant.ofEpochMilli(epochMilli));
        e.setMessage(message);
        e.setLevel("INFO");
        return e;
    }

    @BeforeEach
    void setUp() {
        LogMergeConfig config = LogMergeConfig.defaults()
                .withStrategy(LogMergeConfig.MergeStrategy.TIMESTAMP_ASC);
        merger = new LogMerger(config);
        merger.registerSource("svcA");
        merger.registerSource("svcB");
    }

    @Test
    void drainEmptyReturnsEmptyList() {
        List<LogEntry> result = merger.drain();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void drainTimestampAscOrdersCorrectly() {
        merger.feed("svcA", entryAt("svcA", 3000, "third"));
        merger.feed("svcB", entryAt("svcB", 1000, "first"));
        merger.feed("svcA", entryAt("svcA", 2000, "second"));

        List<LogEntry> result = merger.drain();
        assertEquals(3, result.size());
        assertEquals("first", result.get(0).getMessage());
        assertEquals("second", result.get(1).getMessage());
        assertEquals("third", result.get(2).getMessage());
    }

    @Test
    void drainTimestampDescOrdersCorrectly() {
        LogMergeConfig config = LogMergeConfig.defaults()
                .withStrategy(LogMergeConfig.MergeStrategy.TIMESTAMP_DESC);
        LogMerger descMerger = new LogMerger(config);
        descMerger.registerSource("svcA");
        descMerger.feed("svcA", entryAt("svcA", 1000, "first"));
        descMerger.feed("svcA", entryAt("svcA", 3000, "third"));

        List<LogEntry> result = descMerger.drain();
        assertEquals("third", result.get(0).getMessage());
        assertEquals("first", result.get(1).getMessage());
    }

    @Test
    void dropDuplicatesFiltersIdenticalEntries() {
        LogMergeConfig config = LogMergeConfig.defaults().withDropDuplicates(true);
        LogMerger dedupMerger = new LogMerger(config);
        dedupMerger.registerSource("svcA");

        LogEntry e1 = entryAt("svcA", 1000, "duplicate");
        LogEntry e2 = entryAt("svcA", 1000, "duplicate");
        dedupMerger.feed("svcA", e1);
        dedupMerger.feed("svcA", e2);

        List<LogEntry> result = dedupMerger.drain();
        assertEquals(1, result.size());
    }

    @Test
    void roundRobinInterleavesSources() {
        LogMergeConfig config = LogMergeConfig.defaults()
                .withStrategy(LogMergeConfig.MergeStrategy.ROUND_ROBIN);
        LogMerger rrMerger = new LogMerger(config);
        rrMerger.registerSource("svcA");
        rrMerger.registerSource("svcB");
        rrMerger.feed("svcA", entryAt("svcA", 1000, "A1"));
        rrMerger.feed("svcA", entryAt("svcA", 2000, "A2"));
        rrMerger.feed("svcB", entryAt("svcB", 1500, "B1"));

        List<LogEntry> result = rrMerger.drain();
        assertEquals(3, result.size());
    }

    @Test
    void registerSourceThrowsWhenMaxExceeded() {
        LogMergeConfig config = LogMergeConfig.defaults().withMaxSources(2);
        LogMerger limitedMerger = new LogMerger(config);
        limitedMerger.registerSource("s1");
        limitedMerger.registerSource("s2");
        assertThrows(IllegalStateException.class, () -> limitedMerger.registerSource("s3"));
    }

    @Test
    void feedUnknownSourceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> merger.feed("unknown", entryAt("unknown", 1000, "msg")));
    }

    @Test
    void getSourceCountReturnsCorrectValue() {
        assertEquals(2, merger.getSourceCount());
    }

    @Test
    void configDefaultsAreValid() {
        LogMergeConfig cfg = LogMergeConfig.defaults();
        assertEquals(LogMergeConfig.MergeStrategy.TIMESTAMP_ASC, cfg.getStrategy());
        assertEquals(0L, cfg.getTimestampToleranceMs());
        assertFalse(cfg.isDropDuplicates());
        assertEquals(16, cfg.getMaxSources());
        assertEquals("timestamp", cfg.getTimestampField());
    }
}
