package com.logpulse.dispatch;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogDispatcherTest {

    private LogEntry infoEntry;
    private LogEntry errorEntry;

    @BeforeEach
    void setUp() {
        infoEntry  = new LogEntry("svc-a", "INFO",  "hello world", Instant.now(), Map.of());
        errorEntry = new LogEntry("svc-b", "ERROR", "boom",        Instant.now(), Map.of());
    }

    @Test
    void broadcast_sendsToAllMatchingTargets() {
        List<String> received = new ArrayList<>();
        DispatchConfig cfg = DispatchConfig.builder()
                .strategy(DispatchConfig.Strategy.BROADCAST)
                .addTarget("t1").addTarget("t2").build();
        LogDispatcher dispatcher = new LogDispatcher(cfg);
        dispatcher.registerTarget(new DispatchTarget("t1", e -> received.add("t1:" + e.getMessage())));
        dispatcher.registerTarget(new DispatchTarget("t2", e -> received.add("t2:" + e.getMessage())));

        dispatcher.dispatch(infoEntry);

        assertEquals(2, received.size());
        assertTrue(received.contains("t1:hello world"));
        assertTrue(received.contains("t2:hello world"));
    }

    @Test
    void broadcast_respectsTargetPredicate() {
        List<String> received = new ArrayList<>();
        DispatchConfig cfg = DispatchConfig.builder()
                .strategy(DispatchConfig.Strategy.BROADCAST)
                .addTarget("errors").addTarget("all").build();
        LogDispatcher dispatcher = new LogDispatcher(cfg);
        dispatcher.registerTarget(new DispatchTarget("errors", e -> "ERROR".equals(e.getLevel()), e -> received.add("errors")));
        dispatcher.registerTarget(new DispatchTarget("all",    e -> received.add("all")));

        dispatcher.dispatch(infoEntry);

        assertEquals(1, received.size());
        assertEquals("all", received.get(0));
    }

    @Test
    void roundRobin_alternatesTargets() {
        List<String> received = new ArrayList<>();
        DispatchConfig cfg = DispatchConfig.builder()
                .strategy(DispatchConfig.Strategy.ROUND_ROBIN)
                .addTarget("t1").addTarget("t2").build();
        LogDispatcher dispatcher = new LogDispatcher(cfg);
        dispatcher.registerTarget(new DispatchTarget("t1", e -> received.add("t1")));
        dispatcher.registerTarget(new DispatchTarget("t2", e -> received.add("t2")));

        dispatcher.dispatch(infoEntry);
        dispatcher.dispatch(infoEntry);
        dispatcher.dispatch(infoEntry);

        assertEquals(List.of("t1", "t2", "t1"), received);
    }

    @Test
    void firstMatch_stopsAtFirstMatchingTarget() {
        List<String> received = new ArrayList<>();
        DispatchConfig cfg = DispatchConfig.builder()
                .strategy(DispatchConfig.Strategy.FIRST_MATCH)
                .addTarget("t1").addTarget("t2").build();
        LogDispatcher dispatcher = new LogDispatcher(cfg);
        dispatcher.registerTarget(new DispatchTarget("t1", e -> received.add("t1")));
        dispatcher.registerTarget(new DispatchTarget("t2", e -> received.add("t2")));

        dispatcher.dispatch(infoEntry);

        assertEquals(1, received.size());
        assertEquals("t1", received.get(0));
    }

    @Test
    void dispatch_nullEntryIsIgnored() {
        DispatchConfig cfg = DispatchConfig.builder().addTarget("t1").build();
        LogDispatcher dispatcher = new LogDispatcher(cfg);
        dispatcher.registerTarget(new DispatchTarget("t1", e -> fail("should not be called")));
        assertDoesNotThrow(() -> dispatcher.dispatch(null));
    }

    @Test
    void retries_andThrowsDispatchException() {
        List<Integer> attempts = new ArrayList<>();
        DispatchConfig cfg = DispatchConfig.builder()
                .addTarget("bad").maxRetries(2).failFast(false).build();
        LogDispatcher dispatcher = new LogDispatcher(cfg);
        dispatcher.registerTarget(new DispatchTarget("bad", e -> {
            attempts.add(1);
            throw new RuntimeException("fail");
        }));

        DispatchException ex = assertThrows(DispatchException.class, () -> dispatcher.dispatch(infoEntry));
        assertEquals("bad", ex.getTargetName());
        assertEquals(3, attempts.size()); // 1 initial + 2 retries
    }

    @Test
    void configBuilder_throwsWhenNoTargets() {
        assertThrows(IllegalStateException.class, () ->
                DispatchConfig.builder().build());
    }
}
