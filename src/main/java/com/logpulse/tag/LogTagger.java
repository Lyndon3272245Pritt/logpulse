package com.logpulse.tag;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Applies configurable tags to log entries based on field matching rules.
 * Tags are added to the entry's metadata for downstream routing and filtering.
 */
public class LogTagger {

    private final TagConfig config;
    private final Map<String, Pattern> compiledPatterns;

    public LogTagger(TagConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("TagConfig must not be null");
        }
        this.config = config;
        this.compiledPatterns = new LinkedHashMap<>();
        for (TagRule rule : config.getRules()) {
            if (rule.getPattern() != null && !rule.getPattern().isEmpty()) {
                compiledPatterns.put(rule.getTag(), Pattern.compile(rule.getPattern()));
            }
        }
    }

    /**
     * Evaluates all tag rules against the given log entry and returns a new
     * entry with matching tags applied.
     *
     * @param entry the original log entry
     * @return a log entry with tags applied (may be the same instance if no tags matched)
     */
    public LogEntry tag(LogEntry entry) {
        if (entry == null) {
            return null;
        }

        Set<String> appliedTags = new LinkedHashSet<>(entry.getTags());

        for (TagRule rule : config.getRules()) {
            if (matches(entry, rule)) {
                appliedTags.add(rule.getTag());
            }
        }

        if (appliedTags.equals(new LinkedHashSet<>(entry.getTags()))) {
            return entry;
        }

        return entry.withTags(new ArrayList<>(appliedTags));
    }

    /**
     * Tags a batch of log entries.
     *
     * @param entries list of entries to tag
     * @return list of tagged entries
     */
    public List<LogEntry> tagAll(List<LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<LogEntry> result = new ArrayList<>(entries.size());
        for (LogEntry entry : entries) {
            result.add(tag(entry));
        }
        return result;
    }

    private boolean matches(LogEntry entry, TagRule rule) {
        String field = rule.getField();
        String value = resolveField(entry, field);
        if (value == null) {
            return false;
        }
        Pattern pattern = compiledPatterns.get(rule.getTag());
        if (pattern != null) {
            return pattern.matcher(value).find();
        }
        return rule.getValue() != null && value.equalsIgnoreCase(rule.getValue());
    }

    private String resolveField(LogEntry entry, String field) {
        if (field == null) return null;
        switch (field.toLowerCase()) {
            case "level":   return entry.getLevel();
            case "service": return entry.getService();
            case "message": return entry.getMessage();
            default:        return entry.getFields() != null ? entry.getFields().get(field) : null;
        }
    }

    public TagConfig getConfig() {
        return config;
    }
}
