package com.logpulse.classify;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Classifies log entries into named categories based on configurable rules.
 * Rules are evaluated in priority order; the first match wins.
 */
public class LogClassifier {

    private final List<ClassifyRule> rules;
    private final String defaultCategory;

    public LogClassifier(List<ClassifyRule> rules, String defaultCategory) {
        if (rules == null) {
            throw new IllegalArgumentException("Rules list must not be null");
        }
        this.rules = new ArrayList<>(rules);
        this.rules.sort(Comparator.comparingInt(ClassifyRule::getPriority));
        this.defaultCategory = defaultCategory != null ? defaultCategory : "UNCATEGORIZED";
    }

    /**
     * Classifies a single log entry and returns the matched category name.
     *
     * @param entry the log entry to classify
     * @return the category name, never null
     */
    public String classify(LogEntry entry) {
        if (entry == null) {
            return defaultCategory;
        }
        for (ClassifyRule rule : rules) {
            if (matches(rule, entry)) {
                return rule.getCategory();
            }
        }
        return defaultCategory;
    }

    /**
     * Classifies a batch of log entries and groups them by category.
     *
     * @param entries the log entries to classify
     * @return a map of category name to list of matching entries
     */
    public Map<String, List<LogEntry>> classifyAll(List<LogEntry> entries) {
        Map<String, List<LogEntry>> result = new LinkedHashMap<>();
        if (entries == null) {
            return result;
        }
        for (LogEntry entry : entries) {
            String category = classify(entry);
            result.computeIfAbsent(category, k -> new ArrayList<>()).add(entry);
        }
        return result;
    }

    private boolean matches(ClassifyRule rule, LogEntry entry) {
        String target = resolveField(rule.getField(), entry);
        if (target == null) {
            return false;
        }
        Pattern pattern = rule.getCompiledPattern();
        return pattern != null && pattern.matcher(target).find();
    }

    private String resolveField(String field, LogEntry entry) {
        if (field == null) return null;
        switch (field.toLowerCase()) {
            case "message": return entry.getMessage();
            case "level":   return entry.getLevel();
            case "service": return entry.getService();
            default:        return entry.getFields() != null
                                    ? entry.getFields().get(field)
                                    : null;
        }
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public List<ClassifyRule> getRules() {
        return Collections.unmodifiableList(rules);
    }
}
