package com.logpulse.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for a log processing pipeline, defining the ordered
 * sequence of stage names and global pipeline settings.
 */
public class PipelineConfig {

    private final String name;
    private final List<String> stages;
    private final boolean stopOnError;
    private final int maxConcurrency;

    private PipelineConfig(Builder builder) {
        this.name = builder.name;
        this.stages = Collections.unmodifiableList(new ArrayList<>(builder.stages));
        this.stopOnError = builder.stopOnError;
        this.maxConcurrency = builder.maxConcurrency;
    }

    public String getName() {
        return name;
    }

    public List<String> getStages() {
        return stages;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private final List<String> stages = new ArrayList<>();
        private boolean stopOnError = true;
        private int maxConcurrency = 1;

        private Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Pipeline name must not be blank");
            }
            this.name = name;
        }

        public Builder addStage(String stageName) {
            if (stageName != null && !stageName.isBlank()) {
                stages.add(stageName);
            }
            return this;
        }

        public Builder stopOnError(boolean stopOnError) {
            this.stopOnError = stopOnError;
            return this;
        }

        public Builder maxConcurrency(int maxConcurrency) {
            if (maxConcurrency < 1) {
                throw new IllegalArgumentException("maxConcurrency must be >= 1");
            }
            this.maxConcurrency = maxConcurrency;
            return this;
        }

        public PipelineConfig build() {
            if (stages.isEmpty()) {
                throw new IllegalStateException("Pipeline must have at least one stage");
            }
            return new PipelineConfig(this);
        }
    }
}
