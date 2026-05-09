package com.logpulse.drain;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LogDrainerTest {

    private LogDrainer drainer;
    private List<LogEntry> received;
    private DrainConfig config;

    @BeforeEach
    void setUp() {
        received = new CopyOnWriteArrayList<>();
        config = DrainConfig.builder()
                .batchSize(5)
                .maxQueueSize(50)
                .drainIntervalMs(100)
                .build();
        drainer = new LogDrainer(config, batch -> received.addAll(batch));
    }

    @AfterEach
    void tearDown() {
        drainer.stop();
    }

    private LogEntry entry(String service) {
        return new LogEntry(Instant.now(), "INFO", service, "test message", Map.of());
    }

    @Test
    void offer_acceptsEntryAndQueues() {
        drainer.start();
        assertTrue(drainer.offer(entry("svc-a")));
        assertTrue(drainer.queueSize() <= 1);
    }

    @Test
    void offer_rejectsNullEntry() {
        drainer.start();
        assertFalse(drainer.offer(null));
        assertEquals(0, drainer.queueSize());
    }

    @Test
    void flush_drainsAllQueuedEntries() {
        drainer.start();
        for (int i = 0; i < 3; i++) drainer.offer(entry("svc-b"));
        drainer.flush();
        assertEquals(3, received.size());
        assertEquals(0, drainer.queueSize());
    }

    @Test
    void drainLoop_automaticallyDrainsOnInterval() throws InterruptedException {
        drainer.start();
        drainer.offer(entry("svc-c"));
        Thread.sleep(300);
        assertFalse(received.isEmpty());
    }

    @Test
    void offer_rejectsWhenQueueFull() {
        DrainConfig smallConfig = DrainConfig.builder()
                .batchSize(100)
                .maxQueueSize(2)
                .drainIntervalMs(10000)
                .build();
        LogDrainer smallDrainer = new LogDrainer(smallConfig, batch -> {});
        assertTrue(smallDrainer.offer(entry("x")));
        assertTrue(smallDrainer.offer(entry("x")));
        assertFalse(smallDrainer.offer(entry("x")));
        smallDrainer.stop();
    }

    @Test
    void stop_flushesRemainingEntries() {
        drainer.start();
        drainer.offer(entry("svc-d"));
        drainer.offer(entry("svc-d"));
        drainer.stop();
        assertEquals(2, received.size());
    }

    @Test
    void constructor_throwsOnNullConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogDrainer(null, batch -> {}));
    }

    @Test
    void constructor_throwsOnNullConsumer() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogDrainer(config, null));
    }
}
