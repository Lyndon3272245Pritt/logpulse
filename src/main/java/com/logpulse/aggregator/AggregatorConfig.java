package com.logpulse.aggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable configuration for a {@link LogAggregator}, specifying the
 * log file paths to watch and optional filter parameters.
 */
public class AggregatorConfig {

    private final List<String> filePaths;
    private final String levelFilter;
    private final String serviceFilter;

    private AggregatorConfig(Builder builder) {
        this.filePaths = Collections.unmodifiableList(new ArrayList<>(builder.filePaths));
        this.levelFilter = builder.levelFilter;
        this.serviceFilter = builder.serviceFilter;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public String getLevelFilter() {
        return levelFilter;
    }

    public String getServiceFilter() {
        return serviceFilter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<String> filePaths = new ArrayList<>();
        private String levelFilter;
        private String serviceFilter;

        public Builder addFilePath(String path) {
            filePaths.add(path);
            return this;
        }

        public Builder levelFilter(String level) {
            this.levelFilter = level;
            return this;
        }

        public Builder serviceFilter(String service) {
            this.serviceFilter = service;
            return this;
        }

        public AggregatorConfig build() {
            if (filePaths.isEmpty()) {
                throw new IllegalStateException("At least one file path must be specified.");
            }
            return new AggregatorConfig(this);
        }
    }
}
