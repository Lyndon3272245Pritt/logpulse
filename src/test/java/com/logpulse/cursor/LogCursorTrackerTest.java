package com.logpulse.cursor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LogCursorTrackerTest {

    private CursorConfig config;
    private LogCursorTracker tracker;

    @BeforeEach
    void setUp() {
        config = CursorConfig.builder()
                .storePath("/tmp/cursors")
                .persistOnShutdown(true)
                .flushInterval(Duration.ofSeconds(10))
                .autoResumeOnStart(true)
                .maxTrackedSources(4)
                .build();
        tracker = new LogCursorTracker(config);
    }

    @Test
    void updateAndRetrieveCursor() {
        tracker.updateCursor("service-a", 1024L);
        Optional<Long> cursor = tracker.getCursor("service-a");
        assertTrue(cursor.isPresent());
        assertEquals(1024L, cursor.get());
    }

    @Test
    void unknownSourceReturnsEmpty() {
        Optional<Long> cursor = tracker.getCursor("nonexistent");
        assertFalse(cursor.isPresent());
    }

    @Test
    void updateCursorOverwritesPreviousValue() {
        tracker.updateCursor("service-b", 500L);
        tracker.updateCursor("service-b", 2048L);
        assertEquals(2048L, tracker.getCursor("service-b").orElseThrow());
    }

    @Test
    void removeCursorDeletesEntry() {
        tracker.updateCursor("service-c", 300L);
        tracker.removeCursor("service-c");
        assertFalse(tracker.getCursor("service-c").isPresent());
    }

    @Test
    void snapshotReturnsAllCursors() {
        tracker.updateCursor("svc-1", 100L);
        tracker.updateCursor("svc-2", 200L);
        Map<String, Long> snap = tracker.snapshot();
        assertEquals(2, snap.size());
        assertEquals(100L, snap.get("svc-1"));
        assertEquals(200L, snap.get("svc-2"));
    }

    @Test
    void snapshotIsUnmodifiable() {
        tracker.updateCursor("svc-x", 50L);
        Map<String, Long> snap = tracker.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snap.put("svc-y", 99L));
    }

    @Test
    void resetClearsAllCursors() {
        tracker.updateCursor("a", 1L);
        tracker.updateCursor("b", 2L);
        tracker.reset();
        assertEquals(0, tracker.trackedSourceCount());
    }

    @Test
    void maxTrackedSourcesEnforced() {
        tracker.updateCursor("s1", 1L);
        tracker.updateCursor("s2", 2L);
        tracker.updateCursor("s3", 3L);
        tracker.updateCursor("s4", 4L);
        assertThrows(IllegalStateException.class, () -> tracker.updateCursor("s5", 5L));
    }

    @Test
    void negativeOffsetThrows() {
        assertThrows(IllegalArgumentException.class, () -> tracker.updateCursor("svc", -1L));
    }

    @Test
    void blankSourceIdThrowsOnUpdate() {
        assertThrows(IllegalArgumentException.class, () -> tracker.updateCursor(" ", 10L));
    }

    @Test
    void configDefaultsAreValid() {
        CursorConfig defaults = CursorConfig.builder().build();
        assertEquals(".logpulse/cursors", defaults.getStorePath());
        assertTrue(defaults.isPersistOnShutdown());
        assertTrue(defaults.isAutoResumeOnStart());
        assertEquals(256, defaults.getMaxTrackedSources());
    }
}
