package com.logpulse.fanout;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LogFanoutTest {

    private LogEntry entry;

    @BeforeEach
    void setUp() {
        entry = new LogEntry("auth-service", "INFO", "User logged in");
    }

    @Test
    void dispatchReachesAllSubscribers() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        List<LogEntry> received1 = new ArrayList<>();
        List<LogEntry> received2 = new ArrayList<>();

        fanout.subscribe("s1", received1::add);
        fanout.subscribe("s2", received2::add);
        fanout.dispatch(entry);

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
        assertSame(entry, received1.get(0));
    }

    @Test
    void unsubscribeRemovesSubscriber() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        List<LogEntry> received = new ArrayList<>();
        fanout.subscribe("s1", received::add);
        assertTrue(fanout.unsubscribe("s1"));
        fanout.dispatch(entry);
        assertTrue(received.isEmpty());
    }

    @Test
    void unsubscribeReturnsFalseForUnknown() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        assertFalse(fanout.unsubscribe("ghost"));
    }

    @Test
    void failFastRethrowsOnSubscriberException() {
        FanoutConfig cfg = FanoutConfig.builder().failFast(true).build();
        LogFanout fanout = new LogFanout(cfg);
        fanout.subscribe("boom", e -> { throw new RuntimeException("fail"); });

        assertThrows(FanoutException.class, () -> fanout.dispatch(entry));
    }

    @Test
    void nonFailFastContinuesAfterSubscriberException() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        List<LogEntry> received = new ArrayList<>();
        fanout.subscribe("boom", e -> { throw new RuntimeException("fail"); });
        fanout.subscribe("ok", received::add);

        assertDoesNotThrow(() -> fanout.dispatch(entry));
        assertEquals(1, received.size());
    }

    @Test
    void errorListenerIsNotifiedOnSubscriberFailure() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        fanout.subscribe("boom", e -> { throw new RuntimeException("fail"); });

        AtomicInteger errorCount = new AtomicInteger();
        fanout.addErrorListener((name, e, ex) -> errorCount.incrementAndGet());
        fanout.dispatch(entry);

        assertEquals(1, errorCount.get());
    }

    @Test
    void dispatchNullEntryIsNoOp() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        List<LogEntry> received = new ArrayList<>();
        fanout.subscribe("s1", received::add);
        assertDoesNotThrow(() -> fanout.dispatch(null));
        assertTrue(received.isEmpty());
    }

    @Test
    void subscriberCountReflectsRegistrations() {
        LogFanout fanout = new LogFanout(FanoutConfig.defaults());
        assertEquals(0, fanout.subscriberCount());
        fanout.subscribe("a", e -> {});
        fanout.subscribe("b", e -> {});
        assertEquals(2, fanout.subscriberCount());
        fanout.unsubscribe("a");
        assertEquals(1, fanout.subscriberCount());
    }
}
