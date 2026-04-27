package com.logpulse.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single structured log entry parsed from a service log stream.
 */
public class LogEntry {

    public enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    private final String serviceName;
    private final Level level;
    private final String message;
    private final Instant timestamp;
    private final String rawLine;

    public LogEntry(String serviceName, Level level, String message, Instant timestamp, String rawLine) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
        this.level = Objects.requireNonNull(level, "level must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.rawLine = rawLine != null ? rawLine : message;
    }

    public String getServiceName() { return serviceName; }
    public Level getLevel() { return level; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public String getRawLine() { return rawLine; }

    @Override
    public String toString() {
        return String.format("[%s] [%s] [%s] %s", timestamp, serviceName, level, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogEntry)) return false;
        LogEntry other = (LogEntry) o;
        return Objects.equals(serviceName, other.serviceName)
                && level == other.level
                && Objects.equals(message, other.message)
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, level, message, timestamp);
    }
}
