package com.logpulse.classify;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LogClassifierTest {

    private ClassifyRule errorRule;
    private ClassifyRule timeoutRule;
    private ClassifyRule authRule;
    private LogClassifier classifier;

    @BeforeEach
    void setUp() {
        errorRule   = new ClassifyRule("ERROR_LOG",   "level",   "ERROR",            1);
        timeoutRule = new ClassifyRule("TIMEOUT",     "message", "(?i)timeout",      2);
        authRule    = new ClassifyRule("AUTH_FAILURE","message", "(?i)unauthorized", 3);
        classifier  = new LogClassifier(List.of(errorRule, timeoutRule, authRule), "GENERAL");
    }

    private LogEntry entry(String level, String message, String service) {
        return new LogEntry(Instant.now(), level, message, service, Collections.emptyMap());
    }

    @Test
    void classifiesErrorLevelEntry() {
        LogEntry e = entry("ERROR", "Something broke", "api");
        assertEquals("ERROR_LOG", classifier.classify(e));
    }

    @Test
    void classifiesTimeoutByMessage() {
        LogEntry e = entry("WARN", "Connection Timeout after 30s", "db");
        assertEquals("TIMEOUT", classifier.classify(e));
    }

    @Test
    void classifiesAuthFailureByMessage() {
        LogEntry e = entry("INFO", "401 Unauthorized request", "gateway");
        assertEquals("AUTH_FAILURE", classifier.classify(e));
    }

    @Test
    void returnsDefaultCategoryWhenNoRuleMatches() {
        LogEntry e = entry("INFO", "User logged in", "auth");
        assertEquals("GENERAL", classifier.classify(e));
    }

    @Test
    void respectsPriorityOrder() {
        // An ERROR-level entry that also contains "timeout" should match ERROR_LOG (priority 1)
        LogEntry e = entry("ERROR", "Timeout connecting to DB", "db");
        assertEquals("ERROR_LOG", classifier.classify(e));
    }

    @Test
    void classifyAllGroupsEntriesByCategory() {
        List<LogEntry> entries = List.of(
            entry("ERROR", "NPE", "api"),
            entry("WARN",  "timeout occurred", "db"),
            entry("INFO",  "startup", "app")
        );
        Map<String, List<LogEntry>> grouped = classifier.classifyAll(entries);
        assertEquals(1, grouped.get("ERROR_LOG").size());
        assertEquals(1, grouped.get("TIMEOUT").size());
        assertEquals(1, grouped.get("GENERAL").size());
    }

    @Test
    void classifyNullEntryReturnsDefault() {
        assertEquals("GENERAL", classifier.classify(null));
    }

    @Test
    void classifyAllNullListReturnsEmptyMap() {
        assertTrue(classifier.classifyAll(null).isEmpty());
    }

    @Test
    void constructorThrowsOnNullRules() {
        assertThrows(IllegalArgumentException.class, () -> new LogClassifier(null, "DEFAULT"));
    }

    @Test
    void defaultCategoryFallsBackToUncategorized() {
        LogClassifier c = new LogClassifier(Collections.emptyList(), null);
        assertEquals("UNCATEGORIZED", c.getDefaultCategory());
    }
}
