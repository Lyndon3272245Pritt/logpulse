package com.logpulse.transform;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogTransformerTest {

    private LogTransformer transformer;

    private LogEntry sampleEntry() {
        return new LogEntry("auth-service", "INFO", "User logged in", Instant.now());
    }

    @BeforeEach
    void setUp() {
        transformer = new LogTransformer();
    }

    @Test
    void transformWithNoTransformationsReturnsOriginal() {
        LogEntry entry = sampleEntry();
        LogEntry result = transformer.transform(entry);
        assertSame(entry, result);
    }

    @Test
    void transformAppliesSingleTransformation() {
        transformer.addTransformation(e ->
                new LogEntry(e.getService(), "DEBUG", e.getMessage(), e.getTimestamp()));
        LogEntry result = transformer.transform(sampleEntry());
        assertNotNull(result);
        assertEquals("DEBUG", result.getLevel());
    }

    @Test
    void transformAppliesMultipleTransformationsInOrder() {
        transformer.addTransformation(e ->
                new LogEntry(e.getService().toUpperCase(), e.getLevel(), e.getMessage(), e.getTimestamp()));
        transformer.addTransformation(e ->
                new LogEntry(e.getService(), e.getLevel(), e.getMessage().toLowerCase(), e.getTimestamp()));
        LogEntry result = transformer.transform(sampleEntry());
        assertNotNull(result);
        assertEquals("AUTH-SERVICE", result.getService());
        assertEquals("user logged in", result.getMessage());
    }

    @Test
    void transformReturnsNullWhenStepReturnsNull() {
        transformer.addTransformation(e -> null);
        transformer.addTransformation(e -> {
            fail("Should not reach second transformation");
            return e;
        });
        assertNull(transformer.transform(sampleEntry()));
    }

    @Test
    void transformNullEntryReturnsNull() {
        assertNull(transformer.transform(null));
    }

    @Test
    void transformAllFiltersDiscardedEntries() {
        transformer.addTransformation(e ->
                "ERROR".equals(e.getLevel()) ? null : e);
        List<LogEntry> entries = Arrays.asList(
                new LogEntry("svc", "INFO", "ok", Instant.now()),
                new LogEntry("svc", "ERROR", "fail", Instant.now()),
                new LogEntry("svc", "WARN", "warn", Instant.now())
        );
        List<LogEntry> result = transformer.transformAll(entries);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(e -> "ERROR".equals(e.getLevel())));
    }

    @Test
    void transformationCountReflectsRegisteredSteps() {
        assertEquals(0, transformer.transformationCount());
        transformer.addTransformation(e -> e);
        transformer.addTransformation(e -> e);
        assertEquals(2, transformer.transformationCount());
    }

    @Test
    void clearTransformationsResetsChain() {
        transformer.addTransformation(e -> null);
        transformer.clearTransformations();
        assertEquals(0, transformer.transformationCount());
        assertNotNull(transformer.transform(sampleEntry()));
    }

    @Test
    void addNullTransformationThrows() {
        assertThrows(NullPointerException.class, () -> transformer.addTransformation(null));
    }
}
