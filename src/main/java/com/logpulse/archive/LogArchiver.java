package com.logpulse.archive;

import com.logpulse.model.LogEntry;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Archives log entries to disk, grouped by service, with optional compression and retention eviction.
 */
public class LogArchiver {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private final ArchiveConfig config;
    private final Map<String, List<LogEntry>> pendingByService = new ConcurrentHashMap<>();
    private final List<ArchiveEntry> archiveIndex = new ArrayList<>();

    public LogArchiver(ArchiveConfig config) {
        this.config = config;
    }

    public void buffer(LogEntry entry) {
        pendingByService
                .computeIfAbsent(entry.getService(), k -> new ArrayList<>())
                .add(entry);
    }

    public ArchiveEntry flush(String service) throws IOException {
        List<LogEntry> entries = pendingByService.remove(service);
        if (entries == null || entries.isEmpty()) return null;

        Files.createDirectories(config.getArchiveDirectory());
        String fileName = resolveFileName(service);
        Path target = config.getArchiveDirectory().resolve(fileName);

        List<String> lines = entries.stream()
                .map(e -> e.getTimestamp() + " [" + e.getLevel() + "] " + e.getService() + " - " + e.getMessage())
                .collect(Collectors.toList());
        Files.write(target, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        long size = Files.size(target);
        ArchiveEntry ae = new ArchiveEntry(service, target, Instant.now(), size, config.isCompressOnArchive());
        archiveIndex.add(ae);
        return ae;
    }

    public List<ArchiveEntry> evictExpired() {
        Instant cutoff = Instant.now().minus(config.getRetentionPeriod());
        List<ArchiveEntry> evicted = archiveIndex.stream()
                .filter(e -> e.getArchivedAt().isBefore(cutoff))
                .collect(Collectors.toList());
        evicted.forEach(e -> {
            try { Files.deleteIfExists(e.getFilePath()); } catch (IOException ignored) {}
        });
        archiveIndex.removeAll(evicted);
        return evicted;
    }

    public List<ArchiveEntry> getArchiveIndex() { return List.copyOf(archiveIndex); }

    private String resolveFileName(String service) {
        return config.getFileNamePattern()
                .replace("{date}", DATE_FMT.format(Instant.now()))
                .replace("{service}", service);
    }
}
