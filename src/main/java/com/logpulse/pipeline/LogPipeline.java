package com.logpulse.pipeline;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes an ordered chain of {@link PipelineStage} instances against each
 * incoming {@link LogEntry}.  Stages are applied sequentially; if any stage
 * returns empty the entry is dropped and processing stops for that entry.
 */
public class LogPipeline {

    private static final Logger LOGGER = Logger.getLogger(LogPipeline.class.getName());

    private final PipelineConfig config;
    private final List<PipelineStage> stages;

    public LogPipeline(PipelineConfig config, List<PipelineStage> stages) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(stages, "stages must not be null");
        if (stages.isEmpty()) {
            throw new IllegalArgumentException("Pipeline must contain at least one stage");
        }
        this.config = config;
        this.stages = Collections.unmodifiableList(new ArrayList<>(stages));
    }

    /**
     * Run the entry through every stage in order.
     *
     * @param entry the log entry to process
     * @return the final entry after all stages, or empty if dropped
     */
    public Optional<LogEntry> process(LogEntry entry) {
        Optional<LogEntry> current = Optional.ofNullable(entry);
        for (PipelineStage stage : stages) {
            if (current.isEmpty()) {
                break;
            }
            try {
                current = stage.process(current.get());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING,
                        "Stage '" + stage.stageName() + "' threw an exception for entry: " + entry, ex);
                if (config.isStopOnError()) {
                    return Optional.empty();
                }
                // continue with unchanged entry when stopOnError is false
            }
        }
        return current;
    }

    /**
     * Convenience batch processor.
     *
     * @param entries list of entries to process
     * @return list of entries that survived all stages
     */
    public List<LogEntry> processBatch(List<LogEntry> entries) {
        List<LogEntry> results = new ArrayList<>();
        for (LogEntry e : entries) {
            process(e).ifPresent(results::add);
        }
        return results;
    }

    public PipelineConfig getConfig() {
        return config;
    }

    public List<PipelineStage> getStages() {
        return stages;
    }
}
