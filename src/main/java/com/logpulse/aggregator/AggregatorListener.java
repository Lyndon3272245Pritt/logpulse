package com.logpulse.aggregator;

import com.logpulse.model.LogEntry;

/**
 * Callback interface for receiving aggregated log entries
 * from a {@link LogAggregator}.
 */
@FunctionalInterface
public interface AggregatorListener {

    /**
     * Called whenever a new {@link LogEntry} passes the aggregator's filter
     * and is ready for consumption.
     *
     * @param entry the matched log entry; never {@code null}
     */
    void onLogEntry(LogEntry entry);
}
