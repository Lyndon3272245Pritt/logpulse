package com.logpulse.transform;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Applies a chain of transformations to LogEntry objects.
 * Transformations are applied in order; if any step returns null, the entry is dropped.
 */
public class LogTransformer {

    private final List<Function<LogEntry, LogEntry>> transformations = new ArrayList<>();

    /**
     * Registers a transformation function to the pipeline.
     *
     * @param transformation a function that takes a LogEntry and returns a (possibly modified) LogEntry,
     *                       or null to signal the entry should be discarded.
     */
    public void addTransformation(Function<LogEntry, LogEntry> transformation) {
        Objects.requireNonNull(transformation, "Transformation must not be null");
        transformations.add(transformation);
    }

    /**
     * Applies all registered transformations to the given entry in order.
     *
     * @param entry the original LogEntry
     * @return the transformed LogEntry, or null if any step discarded it
     */
    public LogEntry transform(LogEntry entry) {
        if (entry == null) {
            return null;
        }
        LogEntry current = entry;
        for (Function<LogEntry, LogEntry> transformation : transformations) {
            current = transformation.apply(current);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Applies all registered transformations to a list of entries, discarding nulls.
     *
     * @param entries the list of LogEntry objects to transform
     * @return a new list containing only successfully transformed entries
     */
    public List<LogEntry> transformAll(List<LogEntry> entries) {
        Objects.requireNonNull(entries, "Entries list must not be null");
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : entries) {
            LogEntry transformed = transform(entry);
            if (transformed != null) {
                result.add(transformed);
            }
        }
        return result;
    }

    /**
     * Returns the number of registered transformations.
     */
    public int transformationCount() {
        return transformations.size();
    }

    /**
     * Clears all registered transformations.
     */
    public void clearTransformations() {
        transformations.clear();
    }
}
