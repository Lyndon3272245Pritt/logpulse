package com.logpulse.trace;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LogTraceLinkerTest {

    private TraceConfig config;
    private LogTraceLinker linker;

    @BeforeEach
    void setUp() {
        config = TraceConfig.builder()
                .traceIdField("trace_id")
                .spanIdField("span_id")
                .parentSpanIdField("parent_span_id")
                .propagateContext(true)
                .maxTraceDepth(10)
                .dropOrphanedSpans(false)
                .build();
        linker = new LogTraceLinker(config);
    }

    private LogEntry entryWith(Map<String, String> fields) {
        return new LogEntry("svc", "INFO", "msg", Instant.now(), fields);
    }

    @Test
    void linkReturnsPresentTraceIdWhenFieldPresent() {
        LogEntry entry = entryWith(Map.of("trace_id", "abc123", "span_id", "s1"));
        Optional<String> result = linker.link(entry);
        assertTrue(result.isPresent());
        assertEquals("abc123", result.get());
    }

    @Test
    void linkReturnsEmptyWhenNoTraceId() {
        LogEntry entry = entryWith(Map.of("span_id", "s1"));
        Optional<String> result = linker.link(entry);
        assertFalse(result.isPresent());
    }

    @Test
    void getTraceReturnsAllLinkedEntries() {
        LogEntry e1 = entryWith(Map.of("trace_id", "t1", "span_id", "s1"));
        LogEntry e2 = entryWith(Map.of("trace_id", "t1", "span_id", "s2", "parent_span_id", "s1"));
        linker.link(e1);
        linker.link(e2);
        List<LogEntry> trace = linker.getTrace("t1");
        assertEquals(2, trace.size());
    }

    @Test
    void getTraceReturnsEmptyForUnknownTrace() {
        assertTrue(linker.getTrace("nonexistent").isEmpty());
    }

    @Test
    void activeTraceIdsTracksLinkedTraces() {
        linker.link(entryWith(Map.of("trace_id", "t1")));
        linker.link(entryWith(Map.of("trace_id", "t2")));
        assertTrue(linker.activeTraceIds().containsAll(List.of("t1", "t2")));
    }

    @Test
    void evictRemovesTrace() {
        linker.link(entryWith(Map.of("trace_id", "t1")));
        linker.evict("t1");
        assertFalse(linker.activeTraceIds().contains("t1"));
    }

    @Test
    void dropOrphanedSpansFiltersEntriesWithUnknownParent() {
        TraceConfig strictConfig = TraceConfig.builder()
                .dropOrphanedSpans(true)
                .build();
        LogTraceLinker strictLinker = new LogTraceLinker(strictConfig);

        LogEntry root   = entryWith(Map.of("trace_id", "t1", "span_id", "s1"));
        LogEntry child  = entryWith(Map.of("trace_id", "t1", "span_id", "s2", "parent_span_id", "s1"));
        LogEntry orphan = entryWith(Map.of("trace_id", "t1", "span_id", "s3", "parent_span_id", "ghost"));

        strictLinker.link(root);
        strictLinker.link(child);
        strictLinker.link(orphan);

        List<LogEntry> trace = strictLinker.getTrace("t1");
        assertEquals(2, trace.size());
        assertTrue(trace.contains(root));
        assertTrue(trace.contains(child));
        assertFalse(trace.contains(orphan));
    }

    @Test
    void traceDepthReflectsEntryCount() {
        linker.link(entryWith(Map.of("trace_id", "t1", "span_id", "s1")));
        linker.link(entryWith(Map.of("trace_id", "t1", "span_id", "s2")));
        assertEquals(2, linker.traceDepth("t1"));
    }
}
