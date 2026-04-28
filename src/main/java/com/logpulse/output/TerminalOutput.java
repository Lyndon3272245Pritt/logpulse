package com.logpulse.output;

import com.logpulse.formatter.LogFormatter;
import com.logpulse.model.LogEntry;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles writing formatted log entries to the terminal (stdout/stderr).
 * Supports colorized output and line counting.
 */
public class TerminalOutput {

    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_WHITE  = "\u001B[37m";

    private final PrintStream out;
    private final LogFormatter formatter;
    private final boolean colorEnabled;
    private final AtomicLong lineCount = new AtomicLong(0);

    public TerminalOutput(LogFormatter formatter, boolean colorEnabled) {
        this(System.out, formatter, colorEnabled);
    }

    public TerminalOutput(PrintStream out, LogFormatter formatter, boolean colorEnabled) {
        if (out == null) throw new IllegalArgumentException("PrintStream must not be null");
        if (formatter == null) throw new IllegalArgumentException("LogFormatter must not be null");
        this.out = out;
        this.formatter = formatter;
        this.colorEnabled = colorEnabled;
    }

    public void write(LogEntry entry) {
        if (entry == null) return;
        String formatted = formatter.format(entry);
        if (colorEnabled) {
            formatted = colorize(entry.getLevel(), formatted);
        }
        out.println(formatted);
        lineCount.incrementAndGet();
    }

    public void writeBanner(String message) {
        String banner = "=== " + message + " ===";
        out.println(colorEnabled ? ANSI_CYAN + banner + ANSI_RESET : banner);
    }

    public long getLineCount() {
        return lineCount.get();
    }

    public void resetLineCount() {
        lineCount.set(0);
    }

    private String colorize(String level, String text) {
        if (level == null) return text;
        switch (level.toUpperCase()) {
            case "ERROR":
            case "FATAL":  return ANSI_RED    + text + ANSI_RESET;
            case "WARN":   return ANSI_YELLOW + text + ANSI_RESET;
            case "INFO":   return ANSI_WHITE  + text + ANSI_RESET;
            default:       return text;
        }
    }
}
