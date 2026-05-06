package com.logpulse.alert;

import java.util.Objects;

/**
 * Defines a rule that triggers an alert when matched log conditions are met.
 */
public class AlertRule {

    public enum MatchType {
        LEVEL, MESSAGE_CONTAINS, SERVICE
    }

    private final String ruleId;
    private final MatchType matchType;
    private final String matchValue;
    private final int thresholdCount;
    private final long windowMillis;

    public AlertRule(String ruleId, MatchType matchType, String matchValue,
                     int thresholdCount, long windowMillis) {
        if (ruleId == null || ruleId.isBlank()) throw new IllegalArgumentException("ruleId must not be blank");
        if (matchValue == null || matchValue.isBlank()) throw new IllegalArgumentException("matchValue must not be blank");
        if (thresholdCount < 1) throw new IllegalArgumentException("thresholdCount must be >= 1");
        if (windowMillis < 1) throw new IllegalArgumentException("windowMillis must be >= 1");
        this.ruleId = ruleId;
        this.matchType = Objects.requireNonNull(matchType, "matchType must not be null");
        this.matchValue = matchValue;
        this.thresholdCount = thresholdCount;
        this.windowMillis = windowMillis;
    }

    public String getRuleId() { return ruleId; }
    public MatchType getMatchType() { return matchType; }
    public String getMatchValue() { return matchValue; }
    public int getThresholdCount() { return thresholdCount; }
    public long getWindowMillis() { return windowMillis; }

    @Override
    public String toString() {
        return String.format("AlertRule{id='%s', type=%s, value='%s', threshold=%d, window=%dms}",
                ruleId, matchType, matchValue, thresholdCount, windowMillis);
    }
}
