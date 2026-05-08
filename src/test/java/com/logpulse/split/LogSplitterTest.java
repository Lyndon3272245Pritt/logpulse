package com.logpulse.split;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogSplitterTest {

    private LogEntry entryForService(String service, String message) {
        return new LogEntry()
                .setTimestamp(Instant.now())
                .setMessage(message)
                .setFields(Map.of("service", service));
    }

    private SplitConfig fieldConfig;

    @BeforeEach
    void setUp() {
        fieldConfig = new SplitConfig()
                .setStrategy(SplitConfig.SplitStrategy.FIELD_VALUE)
                .setSplitField("service");
    }

    @Test
    void splitsIntoCorrectBuckets() {
        LogSplitter splitter = new LogSplitter(fieldConfig);
        splitter.split(entryForService("auth", "login ok"));
        splitter.split(entryForService("auth", "login fail"));
        splitter.split(entryForService("payment", "charge ok"));

        assertEquals(2, splitter.getBucket("auth").size());
        assertEquals(1, splitter.getBucket("payment").size());
        assertTrue(splitter.getBucketNames().containsAll(List.of("auth", "payment")));
    }

    @Test
    void unmatchedFieldGoesToUnmatchedBucket() {
        LogSplitter splitter = new LogSplitter(fieldConfig);
        LogEntry noService = new LogEntry().setMessage("bare").setFields(Map.of());
        String bucket = splitter.split(noService);
        assertEquals("__unmatched__", bucket);
        assertEquals(1, splitter.getBucket("__unmatched__").size());
    }

    @Test
    void dropUnmatchedSkipsEntry() {
        SplitConfig cfg = new SplitConfig().setDropUnmatched(true);
        LogSplitter splitter = new LogSplitter(cfg);
        LogEntry noService = new LogEntry().setMessage("bare").setFields(Map.of());
        assertNull(splitter.split(noService));
        assertTrue(splitter.getBucketNames().isEmpty());
    }

    @Test
    void maxBucketsCapRedirectsToUnmatched() {
        SplitConfig cfg = new SplitConfig().setMaxBuckets(2);
        LogSplitter splitter = new LogSplitter(cfg);
        splitter.split(entryForService("svc-a", "m"));
        splitter.split(entryForService("svc-b", "m"));
        // third distinct service should overflow to unmatched
        String bucket = splitter.split(entryForService("svc-c", "m"));
        assertEquals("__unmatched__", bucket);
    }

    @Test
    void allowedBucketsFiltersOthers() {
        SplitConfig cfg = new SplitConfig()
                .setAllowedBuckets(List.of("auth"))
                .setDropUnmatched(false);
        LogSplitter splitter = new LogSplitter(cfg);
        splitter.split(entryForService("auth", "ok"));
        String bucket = splitter.split(entryForService("payment", "ok"));
        assertEquals("__unmatched__", bucket);
        assertEquals(1, splitter.getBucket("auth").size());
    }

    @Test
    void patternStrategySplitsOnRegexGroup() {
        SplitConfig cfg = new SplitConfig()
                .setStrategy(SplitConfig.SplitStrategy.PATTERN)
                .setSplitPattern("level=(\\w+)");
        LogSplitter splitter = new LogSplitter(cfg);
        LogEntry e = new LogEntry().setMessage("level=ERROR something bad").setFields(Map.of());
        String bucket = splitter.split(e);
        assertEquals("ERROR", bucket);
        assertEquals(1, splitter.getBucket("ERROR").size());
    }

    @Test
    void resetClearsBuckets() {
        LogSplitter splitter = new LogSplitter(fieldConfig);
        splitter.split(entryForService("auth", "m"));
        splitter.reset();
        assertTrue(splitter.getBucketNames().isEmpty());
    }

    @Test
    void nullEntryReturnsNull() {
        LogSplitter splitter = new LogSplitter(fieldConfig);
        assertNull(splitter.split(null));
    }
}
