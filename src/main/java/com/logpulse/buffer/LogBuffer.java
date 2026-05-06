package com.logpulse.buffer;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A bounded, thread-safe circular buffer for LogEntry objects.
 * Supports draining entries in batches for downstream processing.
 */
public class LogBuffer {

    private final BlockingQueue<LogEntry> queue;
    private final int capacity;
    private volatile boolean overflow = false;

    public LogBuffer(BufferConfig config) {
        this.capacity = config.getCapacity();
        this.queue = new ArrayBlockingQueue<>(this.capacity);
    }

    /**
     * Offers a log entry to the buffer. If the buffer is full, the oldest
     * entry is dropped and the overflow flag is set.
     */
    public boolean offer(LogEntry entry) {
        if (entry == null) return false;
        boolean added = queue.offer(entry);
        if (!added) {
            overflow = true;
            queue.poll(); // drop oldest
            queue.offer(entry);
        }
        return added;
    }

    /**
     * Drains up to {@code maxEntries} log entries into the provided list.
     * Blocks for up to {@code timeoutMs} milliseconds for the first entry.
     */
    public int drain(List<LogEntry> target, int maxEntries, long timeoutMs)
            throws InterruptedException {
        LogEntry first = queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (first == null) return 0;
        target.add(first);
        return 1 + queue.drainTo(target, maxEntries - 1);
    }

    /** Drains all available entries immediately without blocking. */
    public List<LogEntry> drainAll() {
        List<LogEntry> result = new ArrayList<>();
        queue.drainTo(result);
        return result;
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void resetOverflow() {
        overflow = false;
    }
}
