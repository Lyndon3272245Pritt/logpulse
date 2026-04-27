package com.logpulse.parser;

import com.logpulse.model.LogEntry;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw log lines into {@link LogEntry} instances.
 *
 * Expected format:
 *   2024-01-15T10:23:45.123Z [service-name] [LEVEL] message text
 */
public class LogEntryParser {

    // Pattern: ISO timestamp  [service]  [LEVEL]  message
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\[(\\S+)]\\s+\\[(\\w+)]\\s+(.*)"
    );

    /**
     * Attempts to parse a raw log line.
     *
     * @param rawLine the raw text line from a log stream
     * @return an Optional containing the parsed LogEntry, or empty if parsing fails
     */
    public Optional<LogEntry> parse(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = LOG_PATTERN.matcher(rawLine.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        try {
            Instant timestamp = Instant.parse(matcher.group(1));
            String serviceName = matcher.group(2);
            LogEntry.Level level = LogEntry.Level.valueOf(matcher.group(3).toUpperCase());
            String message = matcher.group(4);

            return Optional.of(new LogEntry(serviceName, level, message, timestamp, rawLine));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
