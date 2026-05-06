package com.logpulse.metrics;

import com.logpulse.model.LogEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Collections;
import java.time.Instant;

/**
 * Collects and tracks runtime metrics for log processing,
 * including counts per service, per level, and throughput stats.
 */
public class LogMetricsCollector {

    private final Map<String, AtomicLong> countByService = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> countByLevel = new ConcurrentHashMap<>();
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalDropped = new AtomicLong(0);
    private final AtomicLong totalFiltered = new AtomicLong(0);
    private final Instant startTime = Instant.now();

    public void recordProcessed(LogEntry entry) {
        if (entry == null) return;
        totalProcessed.incrementAndGet();
        countByService
            .computeIfAbsent(entry.getService(), k -> new AtomicLong(0))
            .incrementAndGet();
        countByLevel
            .computeIfAbsent(entry.getLevel(), k -> new AtomicLong(0))
            .incrementAndGet();
    }

    public void recordDropped() {
        totalDropped.incrementAndGet();
    }

    public void recordFiltered() {
        totalFiltered.incrementAndGet();
    }

    public long getTotalProcessed() {
        return totalProcessed.get();
    }

    public long getTotalDropped() {
        return totalDropped.get();
    }

    public long getTotalFiltered() {
        return totalFiltered.get();
    }

    public Map<String, Long> getCountByService() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        countByService.forEach((k, v) -> snapshot.put(k, v.get()));
        return Collections.unmodifiableMap(snapshot);
    }

    public Map<String, Long> getCountByLevel() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        countByLevel.forEach((k, v) -> snapshot.put(k, v.get()));
        return Collections.unmodifiableMap(snapshot);
    }

    public double getThroughputPerSecond() {
        long elapsedSeconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        if (elapsedSeconds == 0) return totalProcessed.get();
        return (double) totalProcessed.get() / elapsedSeconds;
    }

    public void reset() {
        totalProcessed.set(0);
        totalDropped.set(0);
        totalFiltered.set(0);
        countByService.clear();
        countByLevel.clear();
    }
}
