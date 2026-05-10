package com.logpulse.trace;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Links log entries belonging to the same distributed trace by grouping
 * them under a shared trace ID, respecting parent-child span relationships.
 */
public class LogTraceLinker {

    private final TraceConfig config;
    // traceId -> list of entries in arrival order
    private final Map<String, List<LogEntry>> traceGroups = new ConcurrentHashMap<>();

    public LogTraceLinker(TraceConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Ingests a log entry and registers it under its trace group.
     *
     * @param entry the log entry to link
     * @return the trace ID this entry belongs to, or empty if none found
     */
    public Optional<String> link(LogEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        String traceId = entry.getFields().get(config.getTraceIdField());
        if (traceId == null || traceId.isBlank()) {
            return Optional.empty();
        }
        traceGroups.computeIfAbsent(traceId, k -> Collections.synchronizedList(new ArrayList<>()))
                   .add(entry);
        return Optional.of(traceId);
    }

    /**
     * Returns all entries associated with the given trace ID, ordered by arrival.
     */
    public List<LogEntry> getTrace(String traceId) {
        Objects.requireNonNull(traceId, "traceId must not be null");
        List<LogEntry> entries = traceGroups.getOrDefault(traceId, Collections.emptyList());
        if (config.isDropOrphanedSpans()) {
            return filterOrphans(entries);
        }
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns all known trace IDs currently tracked.
     */
    public Set<String> activeTraceIds() {
        return Collections.unmodifiableSet(traceGroups.keySet());
    }

    /**
     * Evicts the trace group for the given ID, freeing memory.
     */
    public void evict(String traceId) {
        traceGroups.remove(traceId);
    }

    /**
     * Returns the depth (number of entries) for a trace.
     */
    public int traceDepth(String traceId) {
        return traceGroups.getOrDefault(traceId, Collections.emptyList()).size();
    }

    private List<LogEntry> filterOrphans(List<LogEntry> entries) {
        Set<String> knownSpans = new HashSet<>();
        for (LogEntry e : entries) {
            String spanId = e.getFields().get(config.getSpanIdField());
            if (spanId != null) knownSpans.add(spanId);
        }
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry e : entries) {
            String parentSpanId = e.getFields().get(config.getParentSpanIdField());
            // keep root spans (no parent) and spans whose parent is known
            if (parentSpanId == null || parentSpanId.isBlank() || knownSpans.contains(parentSpanId)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
