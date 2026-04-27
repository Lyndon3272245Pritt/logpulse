package com.logpulse.aggregator;

import com.logpulse.filter.LogFilter;
import com.logpulse.model.LogEntry;
import com.logpulse.tail.LogTailer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogAggregatorTest {

    private LogAggregator aggregator;
    private List<LogEntry> received;

    @BeforeEach
    void setUp() {
        received = new ArrayList<>();
        aggregator = new LogAggregator(null);
        aggregator.addListener(received::add);
    }

    @AfterEach
    void tearDown() {
        aggregator.stop();
    }

    @Test
    void addTailer_incrementsTailerCount() {
        LogTailer tailer = mock(LogTailer.class);
        aggregator.addTailer(tailer);
        assertEquals(1, aggregator.getTailerCount());
    }

    @Test
    void start_setsRunningTrue() throws Exception {
        aggregator.start();
        assertTrue(aggregator.isRunning());
    }

    @Test
    void stop_setsRunningFalse() throws Exception {
        aggregator.start();
        aggregator.stop();
        assertFalse(aggregator.isRunning());
    }

    @Test
    void dispatchesEntryToListeners() throws Exception {
        LogTailer tailer = mock(LogTailer.class);
        doAnswer(inv -> {
            BlockingQueue<LogEntry> q = inv.getArgument(0);
            q.put(new LogEntry("svc", "INFO", "hello", Instant.now()));
            return null;
        }).when(tailer).setEntryQueue(any());
        doNothing().when(tailer).start();
        doNothing().when(tailer).stop();

        aggregator.addTailer(tailer);
        aggregator.start();

        Thread.sleep(200);

        assertEquals(1, received.size());
        assertEquals("hello", received.get(0).getMessage());
    }

    @Test
    void filterExcludesNonMatchingEntries() throws Exception {
        LogFilter filter = new LogFilter("ERROR", null);
        LogAggregator filtered = new LogAggregator(filter);
        List<LogEntry> filteredReceived = new ArrayList<>();
        filtered.addListener(filteredReceived::add);

        LogTailer tailer = mock(LogTailer.class);
        doAnswer(inv -> {
            BlockingQueue<LogEntry> q = inv.getArgument(0);
            q.put(new LogEntry("svc", "INFO", "ignored", Instant.now()));
            q.put(new LogEntry("svc", "ERROR", "kept", Instant.now()));
            return null;
        }).when(tailer).setEntryQueue(any());
        doNothing().when(tailer).start();
        doNothing().when(tailer).stop();

        filtered.addTailer(tailer);
        filtered.start();
        Thread.sleep(200);
        filtered.stop();

        assertEquals(1, filteredReceived.size());
        assertEquals("ERROR", filteredReceived.get(0).getLevel());
    }
}
