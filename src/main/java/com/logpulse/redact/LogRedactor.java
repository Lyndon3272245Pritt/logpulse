package com.logpulse.redact;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Applies a chain of {@link RedactRule}s to {@link LogEntry} message fields,
 * scrubbing sensitive data (e.g., tokens, passwords, credit-card numbers)
 * before entries are forwarded to outputs or stored.
 */
public class LogRedactor {

    private final List<RedactRule> rules;

    public LogRedactor(List<RedactRule> rules) {
        Objects.requireNonNull(rules, "rules must not be null");
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    /**
     * Redact the message of the given log entry.
     *
     * @param entry the original log entry
     * @return a new LogEntry whose message has had all rules applied,
     *         or the original entry if no rules are configured
     */
    public LogEntry redact(LogEntry entry) {
        if (entry == null) {
            return null;
        }
        if (rules.isEmpty()) {
            return entry;
        }

        String redacted = entry.getMessage();
        for (RedactRule rule : rules) {
            redacted = rule.apply(redacted);
        }

        if (redacted.equals(entry.getMessage())) {
            return entry;
        }

        return new LogEntry(
                entry.getTimestamp(),
                entry.getLevel(),
                entry.getService(),
                redacted,
                entry.getFields()
        );
    }

    /**
     * Redact a batch of log entries.
     *
     * @param entries list of entries to process
     * @return new list with each entry redacted
     */
    public List<LogEntry> redactAll(List<LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<LogEntry> result = new ArrayList<>(entries.size());
        for (LogEntry entry : entries) {
            result.add(redact(entry));
        }
        return result;
    }

    public List<RedactRule> getRules() {
        return rules;
    }
}
