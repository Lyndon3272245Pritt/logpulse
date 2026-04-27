package com.logpulse.filter;

import java.util.List;
import java.util.regex.Pattern;

/**
 * LogFilter applies configurable criteria to structured log entries,
 * allowing inclusion/exclusion based on level, service name, and message pattern.
 */
public class LogFilter {

    private final List<String> includeLevels;
    private final List<String> includeServices;
    private final Pattern messagePattern;

    public LogFilter(List<String> includeLevels, List<String> includeServices, String messageRegex) {
        this.includeLevels = includeLevels != null ? includeLevels : List.of();
        this.includeServices = includeServices != null ? includeServices : List.of();
        this.messagePattern = (messageRegex != null && !messageRegex.isBlank())
                ? Pattern.compile(messageRegex, Pattern.CASE_INSENSITIVE)
                : null;
    }

    /**
     * Returns true if the given log entry passes all configured filter criteria.
     *
     * @param entry the structured log entry to evaluate
     * @return true if the entry should be included in the output
     */
    public boolean matches(LogEntry entry) {
        if (entry == null) {
            return false;
        }

        if (!includeLevels.isEmpty() && !includeLevels.contains(entry.level().toUpperCase())) {
            return false;
        }

        if (!includeServices.isEmpty() && !includeServices.contains(entry.service())) {
            return false;
        }

        if (messagePattern != null && !messagePattern.matcher(entry.message()).find()) {
            return false;
        }

        return true;
    }

    public List<String> getIncludeLevels() {
        return includeLevels;
    }

    public List<String> getIncludeServices() {
        return includeServices;
    }

    public Pattern getMessagePattern() {
        return messagePattern;
    }
}
