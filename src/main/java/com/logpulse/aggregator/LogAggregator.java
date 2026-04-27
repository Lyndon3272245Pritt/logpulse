package com.logpulse.aggregator;

import com.logpulse.filter.LogFilter;
import com.logpulse.model.LogEntry;
import com.logpulse.tail.LogTailer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Aggregates log entries from multiple LogTailer instances into a single
 * unified stream, applying optional filtering before dispatch.
 */
public class LogAggregator {

    private final BlockingQueue<LogEntry> entryQueue;
    private final LogFilter filter;
    private final List<LogTailer> tailers;
    private final List<AggregatorListener> listeners;
    private volatile boolean running;

    public LogAggregator(LogFilter filter) {
        this.filter = filter;
        this.entryQueue = new LinkedBlockingQueue<>();
        this.tailers = new ArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.running = false;
    }

    public void addTailer(LogTailer tailer) {
        tailers.add(tailer);
        tailer.setEntryQueue(entryQueue);
    }

    public void addListener(AggregatorListener listener) {
        listeners.add(listener);
    }

    public void start() throws IOException {
        running = true;
        for (LogTailer tailer : tailers) {
            tailer.start();
        }
        Thread dispatchThread = new Thread(this::dispatchLoop, "logpulse-dispatcher");
        dispatchThread.setDaemon(true);
        dispatchThread.start();
    }

    public void stop() {
        running = false;
        for (LogTailer tailer : tailers) {
            tailer.stop();
        }
    }

    private void dispatchLoop() {
        while (running) {
            try {
                LogEntry entry = entryQueue.take();
                if (filter == null || filter.matches(entry)) {
                    for (AggregatorListener listener : listeners) {
                        listener.onLogEntry(entry);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public int getTailerCount() {
        return tailers.size();
    }

    public boolean isRunning() {
        return running;
    }
}
