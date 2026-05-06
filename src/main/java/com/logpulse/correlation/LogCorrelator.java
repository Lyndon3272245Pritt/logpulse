package com.logpulse.correlation;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Correlates log entries across multiple services by a shared correlation ID field.
 * Groups related entries within a configurable time window.
 */
public class LogCorrelator {

    private final CorrelationConfig config;
    private final Map<String, List<LogEntry>> groups = new ConcurrentHashMap<>();
    private final Map<String, Long> groupTimestamps = new ConcurrentHashMap<>();

    public LogCorrelator(CorrelationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("CorrelationConfig must not be null");
        }
        this.config = config;
    }

    /**
     * Adds a log entry to the appropriate correlation group.
     *
     * @param entry the log entry to correlate
     */
    public void add(LogEntry entry) {
        if (entry == null) return;
        String correlationId = entry.getFields().get(config.getCorrelationField());
        if (correlationId == null || correlationId.isBlank()) return;

        groups.computeIfAbsent(correlationId, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);
        groupTimestamps.putIfAbsent(correlationId, System.currentTimeMillis());
    }

    /**
     * Returns all entries associated with the given correlation ID.
     *
     * @param correlationId the ID to look up
     * @return list of correlated entries, or empty list if none
     */
    public List<LogEntry> getGroup(String correlationId) {
        return groups.getOrDefault(correlationId, Collections.emptyList());
    }

    /**
     * Evicts correlation groups whose window has expired.
     *
     * @return number of groups evicted
     */
    public int evictExpired() {
        long now = System.currentTimeMillis();
        long windowMs = config.getWindowSeconds() * 1000L;
        List<String> expired = groupTimestamps.entrySet().stream()
                .filter(e -> (now - e.getValue()) > windowMs)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        expired.forEach(id -> {
            groups.remove(id);
            groupTimestamps.remove(id);
        });
        return expired.size();
    }

    /**
     * Returns the number of active correlation groups.
     */
    public int groupCount() {
        return groups.size();
    }

    /**
     * Clears all correlation state.
     */
    public void clear() {
        groups.clear();
        groupTimestamps.clear();
    }
}
