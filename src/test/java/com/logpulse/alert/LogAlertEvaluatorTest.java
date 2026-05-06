package com.logpulse.alert;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogAlertEvaluatorTest {

    private LogAlertEvaluator evaluator;
    private List<AlertEvent> firedAlerts;

    @BeforeEach
    void setUp() {
        evaluator = new LogAlertEvaluator();
        firedAlerts = new ArrayList<>();
        evaluator.setListener(firedAlerts::add);
    }

    private LogEntry entry(String level, String service, String message) {
        return new LogEntry(Instant.now(), level, service, message);
    }

    @Test
    void firesAlertWhenLevelThresholdExceeded() {
        AlertRule rule = new AlertRule("err-rule", AlertRule.MatchType.LEVEL, "ERROR", 3, 60_000);
        evaluator.addRule(rule);

        evaluator.evaluate(entry("ERROR", "svc-a", "something failed"));
        evaluator.evaluate(entry("ERROR", "svc-b", "another failure"));
        assertTrue(firedAlerts.isEmpty(), "Should not fire before threshold");

        evaluator.evaluate(entry("ERROR", "svc-c", "third failure"));
        assertEquals(1, firedAlerts.size());
        assertEquals("err-rule", firedAlerts.get(0).getRule().getRuleId());
    }

    @Test
    void firesAlertOnMessageContains() {
        AlertRule rule = new AlertRule("oom-rule", AlertRule.MatchType.MESSAGE_CONTAINS, "OutOfMemory", 2, 60_000);
        evaluator.addRule(rule);

        evaluator.evaluate(entry("WARN", "svc-a", "java.lang.OutOfMemoryError occurred"));
        evaluator.evaluate(entry("ERROR", "svc-b", "OutOfMemory in heap"));
        assertEquals(1, firedAlerts.size());
    }

    @Test
    void doesNotFireForNonMatchingLevel() {
        AlertRule rule = new AlertRule("warn-rule", AlertRule.MatchType.LEVEL, "WARN", 2, 60_000);
        evaluator.addRule(rule);

        evaluator.evaluate(entry("INFO", "svc-a", "all good"));
        evaluator.evaluate(entry("DEBUG", "svc-a", "debug info"));
        assertTrue(firedAlerts.isEmpty());
    }

    @Test
    void firesAlertOnServiceMatch() {
        AlertRule rule = new AlertRule("svc-rule", AlertRule.MatchType.SERVICE, "payment-service", 2, 60_000);
        evaluator.addRule(rule);

        evaluator.evaluate(entry("ERROR", "payment-service", "timeout"));
        evaluator.evaluate(entry("ERROR", "payment-service", "connection refused"));
        assertEquals(1, firedAlerts.size());
        assertNotNull(firedAlerts.get(0).getSummary());
        assertTrue(firedAlerts.get(0).getSummary().contains("svc-rule"));
    }

    @Test
    void nullEntryIsIgnoredGracefully() {
        AlertRule rule = new AlertRule("null-rule", AlertRule.MatchType.LEVEL, "ERROR", 1, 60_000);
        evaluator.addRule(rule);
        assertDoesNotThrow(() -> evaluator.evaluate(null));
        assertTrue(firedAlerts.isEmpty());
    }

    @Test
    void getRulesReturnsUnmodifiableSnapshot() {
        AlertRule rule = new AlertRule("r1", AlertRule.MatchType.LEVEL, "ERROR", 1, 1000);
        evaluator.addRule(rule);
        List<AlertRule> rules = evaluator.getRules();
        assertEquals(1, rules.size());
        assertThrows(UnsupportedOperationException.class, () -> rules.add(rule));
    }
}
