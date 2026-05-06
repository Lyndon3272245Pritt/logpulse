package com.logpulse.pipeline;

import com.logpulse.model.LogEntry;

import java.util.Optional;

/**
 * Functional interface representing a single processing stage in a log pipeline.
 * A stage receives a {@link LogEntry}, optionally transforms it, and returns
 * an {@link Optional} — empty indicates the entry should be dropped.
 */
@FunctionalInterface
public interface PipelineStage {

    /**
     * Process the given log entry.
     *
     * @param entry the incoming log entry
     * @return the (possibly transformed) entry, or empty to drop it
     */
    Optional<LogEntry> process(LogEntry entry);

    /**
     * Returns a human-readable name for this stage, used in diagnostics.
     * Defaults to the class simple name.
     */
    default String stageName() {
        return getClass().getSimpleName();
    }
}
