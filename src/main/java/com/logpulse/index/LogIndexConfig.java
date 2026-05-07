package com.logpulse.index;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the LogIndexer, controlling which fields are indexed
 * and how the index behaves (capacity, TTL, etc.).
 */
public class LogIndexConfig {

    private final Set<String> indexedFields;
    private final int maxIndexSize;
    private final long entryTtlMillis;
    private final boolean caseSensitive;

    private LogIndexConfig(Builder builder) {
        this.indexedFields = Collections.unmodifiableSet(new HashSet<>(builder.indexedFields));
        this.maxIndexSize = builder.maxIndexSize;
        this.entryTtlMillis = builder.entryTtlMillis;
        this.caseSensitive = builder.caseSensitive;
    }

    public Set<String> getIndexedFields() {
        return indexedFields;
    }

    public int getMaxIndexSize() {
        return maxIndexSize;
    }

    public long getEntryTtlMillis() {
        return entryTtlMillis;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<String> indexedFields = new HashSet<>();
        private int maxIndexSize = 100_000;
        private long entryTtlMillis = 60_000L;
        private boolean caseSensitive = false;

        public Builder indexedField(String field) {
            this.indexedFields.add(field);
            return this;
        }

        public Builder indexedFields(Set<String> fields) {
            this.indexedFields.addAll(fields);
            return this;
        }

        public Builder maxIndexSize(int maxIndexSize) {
            if (maxIndexSize <= 0) throw new IllegalArgumentException("maxIndexSize must be positive");
            this.maxIndexSize = maxIndexSize;
            return this;
        }

        public Builder entryTtlMillis(long ttl) {
            if (ttl <= 0) throw new IllegalArgumentException("entryTtlMillis must be positive");
            this.entryTtlMillis = ttl;
            return this;
        }

        public Builder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public LogIndexConfig build() {
            if (indexedFields.isEmpty()) {
                throw new IllegalStateException("At least one indexed field must be specified");
            }
            return new LogIndexConfig(this);
        }
    }
}
