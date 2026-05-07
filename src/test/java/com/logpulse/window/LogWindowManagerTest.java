package com.logpulse.window;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogWindowManagerTest {

    private LogEntry entryAt(Instant ts) {
        return new LogEntry("svc", "INFO", "msg", ts);
    }

    @Test
    void testFixedWindowFlushesOnExpiry() {
        List<List<LogEntry>> emitted = new ArrayList<>();
        WindowConfig config = WindowConfig.builder()
                .windowType(WindowConfig.WindowType.FIXED)
                .windowSize(Duration.ofSeconds(5))
                .build();
        LogWindowManager manager = new LogWindowManager(config, emitted::add);

        Instant base = Instant.now();
        manager.accept(entryAt(base));
        manager.accept(entryAt(base.plusSeconds(2)));
        // This entry is past the 5s window, should trigger flush
        manager.accept(entryAt(base.plusSeconds(6)));

        assertEquals(1, emitted.size());
        assertEquals(2, emitted.get(0).size());
    }

    @Test
    void testMaxEntriesCapTriggersFlush() {
        List<List<LogEntry>> emitted = new ArrayList<>();
        WindowConfig config = WindowConfig.builder()
                .windowSize(Duration.ofMinutes(1))
                .maxEntriesPerWindow(3)
                .build();
        LogWindowManager manager = new LogWindowManager(config, emitted::add);

        Instant base = Instant.now();
        manager.accept(entryAt(base));
        manager.accept(entryAt(base.plusSeconds(1)));
        manager.accept(entryAt(base.plusSeconds(2)));
        // 4th entry triggers flush of previous 3
        manager.accept(entryAt(base.plusSeconds(3)));

        assertEquals(1, emitted.size());
        assertEquals(3, emitted.get(0).size());
        assertEquals(1, manager.getCurrentWindowSize());
    }

    @Test
    void testExplicitFlushEmitsCurrentWindow() {
        List<List<LogEntry>> emitted = new ArrayList<>();
        WindowConfig config = WindowConfig.builder().windowSize(Duration.ofMinutes(1)).build();
        LogWindowManager manager = new LogWindowManager(config, emitted::add);

        manager.accept(entryAt(Instant.now()));
        manager.flush();

        assertEquals(1, emitted.size());
        assertEquals(0, manager.getCurrentWindowSize());
    }

    @Test
    void testFlushOnEmptyWindowDoesNotEmit() {
        List<List<LogEntry>> emitted = new ArrayList<>();
        WindowConfig config = WindowConfig.builder().windowSize(Duration.ofSeconds(5)).build();
        LogWindowManager manager = new LogWindowManager(config, emitted::add);

        manager.flush();
        assertTrue(emitted.isEmpty());
    }

    @Test
    void testSlidingWindowConfigValidation() {
        assertThrows(IllegalArgumentException.class, () ->
            WindowConfig.builder()
                .windowType(WindowConfig.WindowType.SLIDING)
                .windowSize(Duration.ofSeconds(5))
                .slideInterval(Duration.ofSeconds(5)) // must be less than windowSize
                .build()
        );
    }

    @Test
    void testNullEntryIsIgnored() {
        WindowConfig config = WindowConfig.builder().windowSize(Duration.ofSeconds(5)).build();
        LogWindowManager manager = new LogWindowManager(config, w -> {});
        assertDoesNotThrow(() -> manager.accept(null));
        assertEquals(0, manager.getCurrentWindowSize());
    }
}
