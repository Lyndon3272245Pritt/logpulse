package com.logpulse.partition;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Partitions log entries into named buckets based on a configurable key extractor.
 * Supports round-robin, hash-based, and custom partitioning strategies.
 */
public class LogPartitioner {

    private final PartitionConfig config;
    private final Map<String, List<LogEntry>> partitions;
    private final Function<LogEntry, String> keyExtractor;
    private final Map<String, Integer> partitionCounts;

    public LogPartitioner(PartitionConfig config) {
        this.config = Objects.requireNonNull(config, "PartitionConfig must not be null");
        this.partitions = new ConcurrentHashMap<>();
        this.partitionCounts = new ConcurrentHashMap<>();
        this.keyExtractor = resolveKeyExtractor(config);
    }

    public String partition(LogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("LogEntry must not be null");
        }
        String key = keyExtractor.apply(entry);
        String partitionName = mapToPartition(key);
        partitions.computeIfAbsent(partitionName, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);
        partitionCounts.merge(partitionName, 1, Integer::sum);
        return partitionName;
    }

    public List<LogEntry> getPartition(String name) {
        return Collections.unmodifiableList(partitions.getOrDefault(name, Collections.emptyList()));
    }

    public Set<String> getPartitionNames() {
        return Collections.unmodifiableSet(partitions.keySet());
    }

    public Map<String, Integer> getPartitionCounts() {
        return Collections.unmodifiableMap(partitionCounts);
    }

    public void clearPartition(String name) {
        partitions.remove(name);
        partitionCounts.remove(name);
    }

    public void clearAll() {
        partitions.clear();
        partitionCounts.clear();
    }

    private String mapToPartition(String key) {
        List<String> names = config.getPartitionNames();
        if (names == null || names.isEmpty()) {
            return key;
        }
        int index = Math.abs(key.hashCode()) % names.size();
        return names.get(index);
    }

    private Function<LogEntry, String> resolveKeyExtractor(PartitionConfig cfg) {
        switch (cfg.getStrategy()) {
            case SERVICE:
                return entry -> entry.getService() != null ? entry.getService() : "unknown";
            case LEVEL:
                return entry -> entry.getLevel() != null ? entry.getLevel() : "unknown";
            case CUSTOM:
                return cfg.getCustomKeyExtractor() != null ? cfg.getCustomKeyExtractor()
                        : entry -> "default";
            default:
                return entry -> entry.getService() != null ? entry.getService() : "default";
        }
    }
}
