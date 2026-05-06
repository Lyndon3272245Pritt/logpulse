package com.logpulse.snapshot;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Configuration for periodic log snapshot captures.
 */
public class SnapshotConfig {

    private final Path outputDirectory;
    private final Duration interval;
    private final int maxSnapshots;
    private final boolean compressOnWrite;

    private SnapshotConfig(Builder builder) {
        this.outputDirectory = builder.outputDirectory;
        this.interval = builder.interval;
        this.maxSnapshots = builder.maxSnapshots;
        this.compressOnWrite = builder.compressOnWrite;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Duration getInterval() {
        return interval;
    }

    public int getMaxSnapshots() {
        return maxSnapshots;
    }

    public boolean isCompressOnWrite() {
        return compressOnWrite;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Path outputDirectory = Paths.get("snapshots");
        private Duration interval = Duration.ofMinutes(5);
        private int maxSnapshots = 10;
        private boolean compressOnWrite = false;

        public Builder outputDirectory(Path outputDirectory) {
            if (outputDirectory == null) throw new IllegalArgumentException("outputDirectory must not be null");
            this.outputDirectory = outputDirectory;
            return this;
        }

        public Builder interval(Duration interval) {
            if (interval == null || interval.isNegative() || interval.isZero())
                throw new IllegalArgumentException("interval must be positive");
            this.interval = interval;
            return this;
        }

        public Builder maxSnapshots(int maxSnapshots) {
            if (maxSnapshots < 1) throw new IllegalArgumentException("maxSnapshots must be >= 1");
            this.maxSnapshots = maxSnapshots;
            return this;
        }

        public Builder compressOnWrite(boolean compressOnWrite) {
            this.compressOnWrite = compressOnWrite;
            return this;
        }

        public SnapshotConfig build() {
            return new SnapshotConfig(this);
        }
    }
}
