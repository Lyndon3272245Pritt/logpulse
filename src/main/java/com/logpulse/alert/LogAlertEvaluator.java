package com.logpulse.alert;

import com.logpulse.model.LogEntry;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Evaluates incoming log entries against registered alert rules using a sliding
 * time window. Fires an AlertEvent when a rule's threshold is breached.
 */
public class LogAlertEvaluator {

    private final List<AlertRule> rules = new ArrayList<>();
    private final Map<String, Deque<Long>> windowTimestamps = new ConcurrentHashMap<>();
    private AlertListener listener;

    public void addRule(AlertRule rule) {
        rules.add(rule);
        windowTimestamps.put(rule.getRuleId(), new ArrayDeque<>());
    }

    public void setListener(AlertListener listener) {
        this.listener = listener;
    }

    public void evaluate(LogEntry entry) {
        if (entry == null) return;
        long now = System.currentTimeMillis();
        for (AlertRule rule : rules) {
            if (matches(rule, entry)) {
                Deque<Long> timestamps = windowTimestamps.get(rule.getRuleId());
                timestamps.addLast(now);
                long cutoff = now - rule.getWindowMillis();
                while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                    timestamps.pollFirst();
                }
                if (timestamps.size() >= rule.getThresholdCount()) {
                    timestamps.clear();
                    if (listener != null) {
                        listener.onAlert(new AlertEvent(rule, timestamps.size() + rule.getThresholdCount(), Instant.now()));
                    }
                }
            }
        }
    }

    private boolean matches(AlertRule rule, LogEntry entry) {
        return switch (rule.getMatchType()) {
            case LEVEL -> rule.getMatchValue().equalsIgnoreCase(entry.getLevel());
            case SERVICE -> rule.getMatchValue().equalsIgnoreCase(entry.getService());
            case MESSAGE_CONTAINS -> entry.getMessage() != null &&
                    entry.getMessage().toLowerCase().contains(rule.getMatchValue().toLowerCase());
        };
    }

    public List<AlertRule> getRules() { return List.copyOf(rules); }
}
