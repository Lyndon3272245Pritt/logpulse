package com.logpulse.index;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Maintains an in-memory inverted index over configurable LogEntry fields,
 * enabling fast lookup by field value across multiple services.
 */
public class LogIndexer {

    private final LogIndexConfig config;
    // field -> normalizedValue -> list of (timestamp, entryId)
    private final Map<String, Map<String, List<IndexedRef>>> index = new ConcurrentHashMap<>();
    // entryId -> LogEntry
    private final Map<String, LogEntry> entryStore = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, LogEntry> eldest) {
            return size() > config.getMaxIndexSize();
        }
    };

    public LogIndexer(LogIndexConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public synchronized void index(LogEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        evictExpired();

        String id = entry.getId();
        entryStore.put(id, entry);

        for (String field : config.getIndexedFields()) {
            String raw = entry.getField(field);
            if (raw == null) continue;
            String key = normalize(raw);
            index.computeIfAbsent(field, f -> new ConcurrentHashMap<>())
                 .computeIfAbsent(key, k -> new ArrayList<>())
                 .add(new IndexedRef(System.currentTimeMillis(), id));
        }
    }

    public synchronized List<LogEntry> lookup(String field, String value) {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(value, "value must not be null");
        evictExpired();

        Map<String, List<IndexedRef>> fieldIndex = index.get(field);
        if (fieldIndex == null) return Collections.emptyList();

        List<IndexedRef> refs = fieldIndex.getOrDefault(normalize(value), Collections.emptyList());
        return refs.stream()
                   .map(ref -> entryStore.get(ref.entryId))
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    public synchronized int size() {
        return entryStore.size();
    }

    public synchronized void clear() {
        index.clear();
        entryStore.clear();
    }

    private void evictExpired() {
        long cutoff = System.currentTimeMillis() - config.getEntryTtlMillis();
        index.values().forEach(fieldMap ->
            fieldMap.values().forEach(refs ->
                refs.removeIf(ref -> ref.timestamp < cutoff)
            )
        );
        index.values().forEach(fieldMap -> fieldMap.values().removeIf(List::isEmpty));
    }

    private String normalize(String value) {
        return config.isCaseSensitive() ? value : value.toLowerCase(Locale.ROOT);
    }

    private static class IndexedRef {
        final long timestamp;
        final String entryId;

        IndexedRef(long timestamp, String entryId) {
            this.timestamp = timestamp;
            this.entryId = entryId;
        }
    }
}
