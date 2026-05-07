package com.logpulse.archive;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single archived log file entry with metadata.
 */
public class ArchiveEntry {

    private final String service;
    private final Path filePath;
    private final Instant archivedAt;
    private final long sizeBytes;
    private final boolean compressed;

    public ArchiveEntry(String service, Path filePath, Instant archivedAt, long sizeBytes, boolean compressed) {
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.archivedAt = Objects.requireNonNull(archivedAt, "archivedAt must not be null");
        this.sizeBytes = sizeBytes;
        this.compressed = compressed;
    }

    public String getService() { return service; }
    public Path getFilePath() { return filePath; }
    public Instant getArchivedAt() { return archivedAt; }
    public long getSizeBytes() { return sizeBytes; }
    public boolean isCompressed() { return compressed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArchiveEntry)) return false;
        ArchiveEntry that = (ArchiveEntry) o;
        return Objects.equals(filePath, that.filePath);
    }

    @Override
    public int hashCode() { return Objects.hash(filePath); }

    @Override
    public String toString() {
        return "ArchiveEntry{service='" + service + "', filePath=" + filePath +
               ", archivedAt=" + archivedAt + ", sizeBytes=" + sizeBytes +
               ", compressed=" + compressed + '}';
    }
}
