package com.logpulse.split;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fans a single stream of {@link LogEntry} objects out into named buckets
 * according to a {@link SplitConfig}.  Buckets are lazily created up to
 * {@code maxBuckets}; once the cap is reached every new key is redirected to
 * the configured unmatched bucket (or dropped).
 */
public class LogSplitter {

    private final SplitConfig config;
    private final Map<String, List<LogEntry>> buckets = new ConcurrentHashMap<>();
    private final Set<String> allowedSet;
    private Pattern compiledPattern;

    public LogSplitter(SplitConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.allowedSet = config.getAllowedBuckets().isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(config.getAllowedBuckets());
        if (config.getStrategy() == SplitConfig.SplitStrategy.PATTERN
                && config.getSplitPattern() != null) {
            this.compiledPattern = Pattern.compile(config.getSplitPattern());
        }
    }

    /**
     * Routes {@code entry} into the appropriate bucket.
     *
     * @return the bucket name the entry was placed in, or {@code null} if dropped.
     */
    public String split(LogEntry entry) {
        if (entry == null) return null;

        String bucket = resolveBucket(entry);

        if (bucket == null) {
            if (config.isDropUnmatched()) return null;
            bucket = config.getUnmatchedBucket();
        }

        if (!allowedSet.isEmpty() && !allowedSet.contains(bucket)) {
            if (config.isDropUnmatched()) return null;
            bucket = config.getUnmatchedBucket();
        }

        if (!buckets.containsKey(bucket) && buckets.size() >= config.getMaxBuckets()) {
            if (config.isDropUnmatched()) return null;
            bucket = config.getUnmatchedBucket();
        }

        buckets.computeIfAbsent(bucket, k -> Collections.synchronizedList(new ArrayList<>()))
               .add(entry);
        return bucket;
    }

    /** Returns an unmodifiable view of all entries in the named bucket. */
    public List<LogEntry> getBucket(String name) {
        List<LogEntry> list = buckets.get(name);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /** Returns all current bucket names. */
    public Set<String> getBucketNames() {
        return Collections.unmodifiableSet(buckets.keySet());
    }

    /** Clears all buckets. */
    public void reset() {
        buckets.clear();
    }

    // ------------------------------------------------------------------ //

    private String resolveBucket(LogEntry entry) {
        if (config.getStrategy() == SplitConfig.SplitStrategy.FIELD_VALUE) {
            Object val = entry.getFields().get(config.getSplitField());
            return val != null ? val.toString() : null;
        }
        // PATTERN strategy — match against raw message
        if (compiledPattern != null) {
            String msg = entry.getMessage() != null ? entry.getMessage() : "";
            Matcher m = compiledPattern.matcher(msg);
            if (m.find()) {
                return m.groupCount() > 0 ? m.group(1) : m.group(0);
            }
        }
        return null;
    }
}
