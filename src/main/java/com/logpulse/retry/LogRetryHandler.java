package com.logpulse.retry;

import com.logpulse.model.LogEntry;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles retry logic for log processing operations that may fail transiently.
 */
public class LogRetryHandler {

    private static final Logger LOGGER = Logger.getLogger(LogRetryHandler.class.getName());

    private final RetryConfig config;
    private long totalAttempts = 0;
    private long totalFailures = 0;
    private long totalSuccesses = 0;

    public LogRetryHandler(RetryConfig config) {
        if (config == null) throw new IllegalArgumentException("RetryConfig must not be null");
        this.config = config;
    }

    /**
     * Attempts to process a log entry using the given action, retrying on failure.
     *
     * @param entry   the log entry to process
     * @param action  the action to attempt
     * @return true if the action eventually succeeded, false if all attempts failed
     */
    public boolean attempt(LogEntry entry, Consumer<LogEntry> action) {
        int attempt = 0;
        while (attempt < config.getMaxAttempts()) {
            attempt++;
            totalAttempts++;
            try {
                action.accept(entry);
                totalSuccesses++;
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Attempt " + attempt + " failed for entry [" + entry.getService() + "]: " + e.getMessage());
                if (attempt < config.getMaxAttempts()) {
                    sleep(config.delayForAttempt(attempt).toMillis());
                }
            }
        }
        totalFailures++;
        LOGGER.log(Level.SEVERE, "All " + config.getMaxAttempts() + " attempts exhausted for entry [" + entry.getService() + "]");
        return false;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public long getTotalAttempts() { return totalAttempts; }
    public long getTotalFailures() { return totalFailures; }
    public long getTotalSuccesses() { return totalSuccesses; }

    public void resetStats() {
        totalAttempts = 0;
        totalFailures = 0;
        totalSuccesses = 0;
    }
}
