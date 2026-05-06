package com.logpulse.checkpoint;

import java.util.Objects;

/**
 * Configuration for the {@link CheckpointManager}.
 */
public class CheckpointConfig {

    private static final String DEFAULT_PATH = ".logpulse/checkpoints.properties";

    private final String checkpointFilePath;
    private final boolean autoSaveEnabled;

    private CheckpointConfig(Builder builder) {
        this.checkpointFilePath = builder.checkpointFilePath;
        this.autoSaveEnabled = builder.autoSaveEnabled;
    }

    public String getCheckpointFilePath() {
        return checkpointFilePath;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    @Override
    public String toString() {
        return "CheckpointConfig{path='" + checkpointFilePath + "', autoSave=" + autoSaveEnabled + "}";
    }

    // -------------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String checkpointFilePath = DEFAULT_PATH;
        private boolean autoSaveEnabled = true;

        public Builder checkpointFilePath(String path) {
            this.checkpointFilePath = Objects.requireNonNull(path, "path must not be null");
            return this;
        }

        public Builder autoSaveEnabled(boolean enabled) {
            this.autoSaveEnabled = enabled;
            return this;
        }

        public CheckpointConfig build() {
            if (checkpointFilePath.isBlank()) {
                throw new IllegalArgumentException("checkpointFilePath must not be blank");
            }
            return new CheckpointConfig(this);
        }
    }
}
