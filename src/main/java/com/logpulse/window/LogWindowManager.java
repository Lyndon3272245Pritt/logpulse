package com.logpulse.window;

import com.logpulse.model.LogEntry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Manages fixed or sliding time windows over a stream of LogEntry objects.
 * Emits completed windows to a registered listener.
 */
public class LogWindowManager {

    private final WindowConfig config;
    private final Consumer<List<LogEntry>> windowListener;
    private final List<LogEntry> currentWindow = new CopyOnWriteArrayList<>();
    private volatile Instant windowStart;

    public LogWindowManager(WindowConfig config, Consumer<List<LogEntry>> windowListener) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        if (windowListener == null) throw new IllegalArgumentException("windowListener must not be null");
        this.config = config;
        this.windowListener = windowListener;
        this.windowStart = Instant.now();
    }

    /**
     * Accepts a log entry, adds it to the current window, and flushes if the window has expired
     * or the entry cap has been reached.
     */
    public synchronized void accept(LogEntry entry) {
        if (entry == null) return;

        Instant now = entry.getTimestamp() != null ? entry.getTimestamp() : Instant.now();

        if (isWindowExpired(now) || currentWindow.size() >= config.getMaxEntriesPerWindow()) {
            flush();
            advanceWindow(now);
        }

        currentWindow.add(entry);
    }

    /**
     * Flushes the current window contents to the listener and clears the buffer.
     */
    public synchronized void flush() {
        if (!currentWindow.isEmpty()) {
            windowListener.accept(Collections.unmodifiableList(new ArrayList<>(currentWindow)));
            currentWindow.clear();
        }
    }

    private boolean isWindowExpired(Instant now) {
        return now.isAfter(windowStart.plus(config.getWindowSize()));
    }

    private void advanceWindow(Instant now) {
        if (config.getWindowType() == WindowConfig.WindowType.SLIDING) {
            windowStart = windowStart.plus(config.getSlideInterval());
        } else {
            windowStart = now;
        }
    }

    public Instant getWindowStart() { return windowStart; }

    public int getCurrentWindowSize() { return currentWindow.size(); }
}
