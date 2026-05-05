package com.logpulse.sampling;

import com.logpulse.model.LogEntry;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Rate-based log sampler that allows only a fraction of log entries through,
 * useful for high-volume streams where full output would overwhelm the terminal.
 */
public class LogSampler {

    private final SamplerConfig config;
    private final AtomicLong totalSeen = new AtomicLong(0);
    private final AtomicLong totalAccepted = new AtomicLong(0);

    public LogSampler(SamplerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SamplerConfig must not be null");
        }
        this.config = config;
    }

    /**
     * Determines whether the given log entry should be forwarded downstream.
     *
     * @param entry the log entry to evaluate
     * @return true if the entry passes the sampling criteria
     */
    public boolean sample(LogEntry entry) {
        if (entry == null) {
            return false;
        }

        long seen = totalSeen.incrementAndGet();

        // Always pass entries whose level is in the force-through set
        if (config.isForcedLevel(entry.getLevel())) {
            totalAccepted.incrementAndGet();
            return true;
        }

        // Apply nth-sample strategy: accept every N-th entry
        boolean accepted = (seen % config.getSampleRate()) == 0;
        if (accepted) {
            totalAccepted.incrementAndGet();
        }
        return accepted;
    }

    public long getTotalSeen() {
        return totalSeen.get();
    }

    public long getTotalAccepted() {
        return totalAccepted.get();
    }

    public double getAcceptanceRatio() {
        long seen = totalSeen.get();
        return seen == 0 ? 0.0 : (double) totalAccepted.get() / seen;
    }

    public void reset() {
        totalSeen.set(0);
        totalAccepted.set(0);
    }
}
