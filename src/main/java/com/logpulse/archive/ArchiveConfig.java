package com.logpulse.archive;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Configuration for log archiving behavior.
 */
public class ArchiveConfig {

    private final Path archiveDirectory;
    private final Duration retentionPeriod;
    private final long maxArchiveSizeBytes;
    private final boolean compressOnArchive;
    private final String fileNamePattern;

    private ArchiveConfig(Builder builder) {
        this.archiveDirectory = builder.archiveDirectory;
        this.retentionPeriod = builder.retentionPeriod;
        this.maxArchiveSizeBytes = builder.maxArchiveSizeBytes;
        this.compressOnArchive = builder.compressOnArchive;
        this.fileNamePattern = builder.fileNamePattern;
    }

    public Path getArchiveDirectory() { return archiveDirectory; }
    public Duration getRetentionPeriod() { return retentionPeriod; }
    public long getMaxArchiveSizeBytes() { return maxArchiveSizeBytes; }
    public boolean isCompressOnArchive() { return compressOnArchive; }
    public String getFileNamePattern() { return fileNamePattern; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Path archiveDirectory = Paths.get("archives");
        private Duration retentionPeriod = Duration.ofDays(30);
        private long maxArchiveSizeBytes = 100 * 1024 * 1024L; // 100 MB
        private boolean compressOnArchive = true;
        private String fileNamePattern = "logpulse-{date}-{service}.log";

        public Builder archiveDirectory(Path path) { this.archiveDirectory = path; return this; }
        public Builder retentionPeriod(Duration period) { this.retentionPeriod = period; return this; }
        public Builder maxArchiveSizeBytes(long bytes) { this.maxArchiveSizeBytes = bytes; return this; }
        public Builder compressOnArchive(boolean compress) { this.compressOnArchive = compress; return this; }
        public Builder fileNamePattern(String pattern) { this.fileNamePattern = pattern; return this; }

        public ArchiveConfig build() {
            if (archiveDirectory == null) throw new IllegalStateException("archiveDirectory must not be null");
            if (retentionPeriod == null || retentionPeriod.isNegative()) throw new IllegalStateException("retentionPeriod must be positive");
            if (maxArchiveSizeBytes <= 0) throw new IllegalStateException("maxArchiveSizeBytes must be positive");
            return new ArchiveConfig(this);
        }
    }
}
