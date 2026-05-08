package com.logpulse.merge;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

/**
 * Merges log entries from multiple named sources into a unified, ordered stream.
 * Supports timestamp-based ordering, round-robin, and duplicate dropping.
 */
public class LogMerger {

    private final LogMergeConfig config;
    private final Map<String, Queue<LogEntry>> sourceQueues;
    private final Set<String> seenSignatures;
    private int roundRobinIndex;

    public LogMerger(LogMergeConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
        this.sourceQueues = new LinkedHashMap<>();
        this.seenSignatures = new LinkedHashSet<>();
        this.roundRobinIndex = 0;
    }

    public void registerSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank())
            throw new IllegalArgumentException("sourceId must not be blank");
        if (sourceQueues.size() >= config.getMaxSources())
            throw new IllegalStateException("Maximum source limit reached: " + config.getMaxSources());
        sourceQueues.putIfAbsent(sourceId, new ArrayDeque<>());
    }

    public void feed(String sourceId, LogEntry entry) {
        if (!sourceQueues.containsKey(sourceId))
            throw new IllegalArgumentException("Unknown source: " + sourceId);
        if (entry == null) return;
        sourceQueues.get(sourceId).offer(entry);
    }

    public List<LogEntry> drain() {
        List<LogEntry> result = new ArrayList<>();
        switch (config.getStrategy()) {
            case TIMESTAMP_ASC -> drainByTimestamp(result, false);
            case TIMESTAMP_DESC -> drainByTimestamp(result, true);
            case ROUND_ROBIN -> drainRoundRobin(result);
            case PRIORITY -> drainByTimestamp(result, false); // priority falls back to timestamp
        }
        return result;
    }

    private void drainByTimestamp(List<LogEntry> result, boolean descending) {
        PriorityQueue<LogEntry> pq = new PriorityQueue<>(
                descending
                        ? Comparator.comparing(LogEntry::getTimestamp).reversed()
                        : Comparator.comparing(LogEntry::getTimestamp)
        );
        for (Queue<LogEntry> q : sourceQueues.values()) {
            pq.addAll(q);
            q.clear();
        }
        while (!pq.isEmpty()) {
            LogEntry entry = pq.poll();
            if (shouldInclude(entry)) result.add(entry);
        }
    }

    private void drainRoundRobin(List<LogEntry> result) {
        List<Queue<LogEntry>> queues = new ArrayList<>(sourceQueues.values());
        boolean anyHasData = true;
        while (anyHasData) {
            anyHasData = false;
            for (Queue<LogEntry> q : queues) {
                LogEntry entry = q.poll();
                if (entry != null) {
                    anyHasData = true;
                    if (shouldInclude(entry)) result.add(entry);
                }
            }
        }
    }

    private boolean shouldInclude(LogEntry entry) {
        if (!config.isDropDuplicates()) return true;
        String sig = entry.getTimestamp() + "|" + entry.getMessage();
        return seenSignatures.add(sig);
    }

    public void clearSeen() {
        seenSignatures.clear();
    }

    public int getSourceCount() {
        return sourceQueues.size();
    }

    public LogMergeConfig getConfig() {
        return config;
    }
}
