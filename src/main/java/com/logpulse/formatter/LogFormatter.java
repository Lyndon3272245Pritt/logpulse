package com.logpulse.formatter;

import com.logpulse.model.LogEntry;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Formats LogEntry objects into human-readable terminal output strings.
 * Supports plain text and colorized ANSI output modes.
 */
public class LogFormatter {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_WHITE  = "\u001B[37m";
    private static final String ANSI_BOLD   = "\u001B[1m";

    private final boolean colorEnabled;

    public LogFormatter(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    public String format(LogEntry entry) {
        String timestamp = entry.getTimestamp() != null
                ? entry.getTimestamp().format(DISPLAY_FORMAT)
                : "unknown-time";

        String level   = entry.getLevel() != null ? entry.getLevel().toUpperCase() : "UNKNOWN";
        String service = entry.getService() != null ? entry.getService() : "unknown-service";
        String message = entry.getMessage() != null ? entry.getMessage() : "";

        StringBuilder sb = new StringBuilder();
        sb.append(colorEnabled ? ANSI_WHITE : "").append("[").append(timestamp).append("] ");
        sb.append(colorEnabled ? levelColor(level) : "").append(String.format("%-5s", level));
        sb.append(colorEnabled ? ANSI_RESET : "").append(" ");
        sb.append(colorEnabled ? ANSI_CYAN : "").append("[").append(service).append("]");
        sb.append(colorEnabled ? ANSI_RESET : "").append(" ");
        sb.append(message);

        Map<String, String> fields = entry.getFields();
        if (fields != null && !fields.isEmpty()) {
            sb.append(" {");
            fields.forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
            sb.setCharAt(sb.length() - 1, '}');
        }

        if (colorEnabled) sb.append(ANSI_RESET);
        return sb.toString();
    }

    private String levelColor(String level) {
        switch (level) {
            case "ERROR": return ANSI_BOLD + ANSI_RED;
            case "WARN":  return ANSI_YELLOW;
            default:      return ANSI_WHITE;
        }
    }
}
