package com.logpulse.alert;

import java.time.Instant;

/**
 * Represents a fired alert event produced when an AlertRule threshold is exceeded.
 */
public class AlertEvent {

    private final AlertRule rule;
    private final int matchedCount;
    private final Instant firedAt;
    private final String summary;

    public AlertEvent(AlertRule rule, int matchedCount, Instant firedAt) {
        this.rule = rule;
        this.matchedCount = matchedCount;
        this.firedAt = firedAt;
        this.summary = String.format("[ALERT] Rule '%s' fired: %d matches in %dms window (threshold=%d)",
                rule.getRuleId(), matchedCount, rule.getWindowMillis(), rule.getThresholdCount());
    }

    public AlertRule getRule() { return rule; }
    public int getMatchedCount() { return matchedCount; }
    public Instant getFiredAt() { return firedAt; }
    public String getSummary() { return summary; }

    @Override
    public String toString() { return summary; }
}
