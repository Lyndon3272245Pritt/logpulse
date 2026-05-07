package com.logpulse.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single audit event capturing who did what and when
 * within the logpulse pipeline.
 */
public class AuditEvent {

    public enum Action {
        LOG_INGESTED, LOG_FILTERED, LOG_DROPPED, LOG_TRANSFORMED,
        LOG_DISPATCHED, LOG_ARCHIVED, PIPELINE_STARTED, PIPELINE_STOPPED,
        CONFIG_CHANGED, ALERT_FIRED
    }

    private final String eventId;
    private final Instant timestamp;
    private final Action action;
    private final String source;
    private final String detail;
    private final String correlationId;

    public AuditEvent(String eventId, Instant timestamp, Action action,
                      String source, String detail, String correlationId) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.source = source;
        this.detail = detail;
        this.correlationId = correlationId;
    }

    public String getEventId() { return eventId; }
    public Instant getTimestamp() { return timestamp; }
    public Action getAction() { return action; }
    public String getSource() { return source; }
    public String getDetail() { return detail; }
    public String getCorrelationId() { return correlationId; }

    @Override
    public String toString() {
        return String.format("AuditEvent{id='%s', ts=%s, action=%s, source='%s', detail='%s', correlationId='%s'}",
                eventId, timestamp, action, source, detail, correlationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditEvent)) return false;
        AuditEvent that = (AuditEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
