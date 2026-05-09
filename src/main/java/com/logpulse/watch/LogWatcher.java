package com.logpulse.watch;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Watches one or more directories for new or modified log files matching
 * the configured glob patterns. Notifies a listener with the affected path.
 */
public class LogWatcher implements AutoCloseable {

    private static final Logger log = Logger.getLogger(LogWatcher.class.getName());

    private final WatchConfig config;
    private final Consumer<Path> onFileEvent;
    private final ScheduledExecutorService scheduler;
    private final Map<Path, Instant> lastModifiedCache = new ConcurrentHashMap<>();
    private ScheduledFuture<?> watchTask;

    public LogWatcher(WatchConfig config, Consumer<Path> onFileEvent) {
        this.config = Objects.requireNonNull(config);
        this.onFileEvent = Objects.requireNonNull(onFileEvent);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "logpulse-watcher");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        long millis = config.getPollInterval().toMillis();
        watchTask = scheduler.scheduleAtFixedRate(this::poll, 0, millis, TimeUnit.MILLISECONDS);
        log.info("LogWatcher started — polling every " + millis + "ms");
    }

    public void stop() {
        if (watchTask != null) {
            watchTask.cancel(false);
        }
        log.info("LogWatcher stopped");
    }

    private void poll() {
        for (String rawPath : config.getWatchPaths()) {
            Path dir = Paths.get(rawPath);
            if (!Files.isDirectory(dir)) {
                log.warning("Watch path is not a directory: " + dir);
                continue;
            }
            try {
                scanDirectory(dir);
            } catch (IOException e) {
                log.severe("Error scanning directory " + dir + ": " + e.getMessage());
            }
        }
    }

    private void scanDirectory(Path dir) throws IOException {
        int maxDepth = config.isRecursive() ? Integer.MAX_VALUE : 1;
        Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), maxDepth,
            new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matchesPatterns(file)) {
                        Instant modified = attrs.lastModifiedTime().toInstant();
                        Instant cached = lastModifiedCache.get(file);
                        if (cached == null || modified.isAfter(cached)) {
                            lastModifiedCache.put(file, modified);
                            onFileEvent.accept(file);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
    }

    private boolean matchesPatterns(Path file) {
        String name = file.getFileName().toString();
        boolean included = config.getIncludePatterns().stream()
            .anyMatch(p -> FileSystems.getDefault().getPathMatcher("glob:" + p).matches(Paths.get(name)));
        boolean excluded = config.getExcludePatterns().stream()
            .anyMatch(p -> FileSystems.getDefault().getPathMatcher("glob:" + p).matches(Paths.get(name)));
        return included && !excluded;
    }

    @Override
    public void close() {
        stop();
        scheduler.shutdownNow();
    }
}
