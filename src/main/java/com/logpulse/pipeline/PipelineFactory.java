package com.logpulse.pipeline;

import com.logpulse.filter.LogFilter;
import com.logpulse.model.LogEntry;
import com.logpulse.transform.LogTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory that assembles a {@link LogPipeline} from a {@link PipelineConfig}
 * by mapping well-known stage names to concrete {@link PipelineStage} implementations.
 * Unknown stage names are logged as warnings and skipped.
 */
public class PipelineFactory {

    private final LogFilter filter;
    private final LogTransformer transformer;

    public PipelineFactory(LogFilter filter, LogTransformer transformer) {
        this.filter = filter;
        this.transformer = transformer;
    }

    public LogPipeline create(PipelineConfig config) {
        List<PipelineStage> stages = new ArrayList<>();
        for (String stageName : config.getStages()) {
            PipelineStage stage = resolveStage(stageName);
            if (stage != null) {
                stages.add(stage);
            } else {
                System.err.println("[PipelineFactory] Unknown stage '" + stageName + "' — skipped.");
            }
        }
        if (stages.isEmpty()) {
            throw new IllegalStateException(
                    "Pipeline '" + config.getName() + "' resolved to zero valid stages.");
        }
        return new LogPipeline(config, stages);
    }

    private PipelineStage resolveStage(String name) {
        return switch (name.toLowerCase()) {
            case "filter" -> entry -> filter.matches(entry) ? Optional.of(entry) : Optional.empty();
            case "transform" -> entry -> Optional.ofNullable(transformer.transform(entry));
            case "passthrough" -> Optional::ofNullable;
            default -> null;
        };
    }
}
