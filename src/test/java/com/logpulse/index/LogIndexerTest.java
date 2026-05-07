package com.logpulse.index;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogIndexerTest {

    private LogIndexConfig config;
    private LogIndexer indexer;

    @BeforeEach
    void setUp() {
        config = LogIndexConfig.builder()
                .indexedField("service")
                .indexedField("level")
                .maxIndexSize(500)
                .entryTtlMillis(30_000L)
                .caseSensitive(false)
                .build();
        indexer = new LogIndexer(config);
    }

    private LogEntry mockEntry(String id, String service, String level) {
        LogEntry entry = mock(LogEntry.class);
        when(entry.getId()).thenReturn(id);
        when(entry.getField("service")).thenReturn(service);
        when(entry.getField("level")).thenReturn(level);
        when(entry.getField(anyString())).thenAnswer(inv -> {
            String f = inv.getArgument(0);
            if ("service".equals(f)) return service;
            if ("level".equals(f)) return level;
            return null;
        });
        return entry;
    }

    @Test
    void indexAndLookupByService() {
        LogEntry e1 = mockEntry("id-1", "auth", "ERROR");
        LogEntry e2 = mockEntry("id-2", "auth", "INFO");
        LogEntry e3 = mockEntry("id-3", "gateway", "WARN");

        indexer.index(e1);
        indexer.index(e2);
        indexer.index(e3);

        List<LogEntry> results = indexer.lookup("service", "auth");
        assertEquals(2, results.size());
    }

    @Test
    void lookupIsCaseInsensitive() {
        indexer.index(mockEntry("id-1", "Auth", "INFO"));
        List<LogEntry> results = indexer.lookup("service", "AUTH");
        assertEquals(1, results.size());
    }

    @Test
    void lookupReturnsEmptyForUnknownField() {
        indexer.index(mockEntry("id-1", "auth", "INFO"));
        List<LogEntry> results = indexer.lookup("nonexistent", "auth");
        assertTrue(results.isEmpty());
    }

    @Test
    void lookupReturnsEmptyForUnknownValue() {
        indexer.index(mockEntry("id-1", "auth", "INFO"));
        List<LogEntry> results = indexer.lookup("service", "unknown");
        assertTrue(results.isEmpty());
    }

    @Test
    void clearRemovesAllEntries() {
        indexer.index(mockEntry("id-1", "auth", "INFO"));
        indexer.clear();
        assertEquals(0, indexer.size());
        assertTrue(indexer.lookup("service", "auth").isEmpty());
    }

    @Test
    void configBuilderThrowsOnEmptyFields() {
        assertThrows(IllegalStateException.class, () ->
            LogIndexConfig.builder().maxIndexSize(100).entryTtlMillis(5000).build()
        );
    }

    @Test
    void configBuilderThrowsOnNonPositiveMaxSize() {
        assertThrows(IllegalArgumentException.class, () ->
            LogIndexConfig.builder().indexedField("level").maxIndexSize(0).build()
        );
    }

    @Test
    void indexNullEntryThrows() {
        assertThrows(NullPointerException.class, () -> indexer.index(null));
    }
}
