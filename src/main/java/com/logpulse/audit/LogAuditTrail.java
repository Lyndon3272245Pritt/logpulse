package com.logpulse.audit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe audit trail that records pipeline actions up to a configured
 * maximum number of retained events, evicting the oldest when full.
 */
public class LogAuditTrail {

    private final AuditConfig config;
    private final Deque<AuditEvent> events;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LogAuditTrail(AuditConfig config) {
        this.config = config;
        this.events = new ArrayDeque<>();
    }

    /**
     * Records an audit event if the trail is enabled and the action is tracked.
     */
    public void record(AuditEvent.Action action, String source, String detail, String correlationId) {
        if (!config.isEnabled()) return;
        if (!config.getTrackedActions().contains(action)) return;

        String resolvedCorrelationId = config.isIncludeCorrelationId() ? correlationId : null;
        AuditEvent event = new AuditEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                action,
                source,
                detail,
                resolvedCorrelationId
        );

        lock.writeLock().lock();
        try {
            if (events.size() >= config.getMaxRetainedEvents()) {
                events.pollFirst();
            }
            events.addLast(event);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Returns a snapshot of all retained events in insertion order. */
    public List<AuditEvent> getEvents() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(events);
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Returns events filtered by action type. */
    public List<AuditEvent> getEventsByAction(AuditEvent.Action action) {
        lock.readLock().lock();
        try {
            return events.stream()
                    .filter(e -> e.getAction() == action)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Clears all retained audit events. */
    public void clear() {
        lock.writeLock().lock();
        try {
            events.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return events.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
