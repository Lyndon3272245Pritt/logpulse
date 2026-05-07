package com.logpulse.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogAuditTrailTest {

    private AuditConfig defaultConfig;
    private LogAuditTrail trail;

    @BeforeEach
    void setUp() {
        defaultConfig = AuditConfig.builder().build();
        trail = new LogAuditTrail(defaultConfig);
    }

    @Test
    void recordsEventWhenEnabled() {
        trail.record(AuditEvent.Action.LOG_INGESTED, "service-a", "line 42", "corr-1");
        assertEquals(1, trail.size());
        AuditEvent event = trail.getEvents().get(0);
        assertEquals(AuditEvent.Action.LOG_INGESTED, event.getAction());
        assertEquals("service-a", event.getSource());
        assertEquals("line 42", event.getDetail());
        assertEquals("corr-1", event.getCorrelationId());
    }

    @Test
    void doesNotRecordWhenDisabled() {
        AuditConfig disabledConfig = AuditConfig.builder().enabled(false).build();
        LogAuditTrail disabledTrail = new LogAuditTrail(disabledConfig);
        disabledTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "detail", "corr");
        assertEquals(0, disabledTrail.size());
    }

    @Test
    void doesNotRecordUntrackedAction() {
        AuditConfig config = AuditConfig.builder()
                .trackedActions(EnumSet.of(AuditEvent.Action.ALERT_FIRED))
                .build();
        LogAuditTrail filteredTrail = new LogAuditTrail(config);
        filteredTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "detail", "corr");
        assertEquals(0, filteredTrail.size());
        filteredTrail.record(AuditEvent.Action.ALERT_FIRED, "svc", "alert!", "corr");
        assertEquals(1, filteredTrail.size());
    }

    @Test
    void evictsOldestEventWhenAtCapacity() {
        AuditConfig smallConfig = AuditConfig.builder().maxRetainedEvents(3).build();
        LogAuditTrail smallTrail = new LogAuditTrail(smallConfig);
        smallTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "first", "c1");
        smallTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "second", "c2");
        smallTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "third", "c3");
        smallTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "fourth", "c4");
        assertEquals(3, smallTrail.size());
        List<AuditEvent> events = smallTrail.getEvents();
        assertEquals("second", events.get(0).getDetail());
        assertEquals("fourth", events.get(2).getDetail());
    }

    @Test
    void getEventsByActionFiltersCorrectly() {
        trail.record(AuditEvent.Action.LOG_INGESTED, "svc", "in", "c1");
        trail.record(AuditEvent.Action.LOG_DROPPED, "svc", "drop", "c2");
        trail.record(AuditEvent.Action.LOG_INGESTED, "svc", "in2", "c3");
        List<AuditEvent> ingested = trail.getEventsByAction(AuditEvent.Action.LOG_INGESTED);
        assertEquals(2, ingested.size());
        List<AuditEvent> dropped = trail.getEventsByAction(AuditEvent.Action.LOG_DROPPED);
        assertEquals(1, dropped.size());
    }

    @Test
    void clearRemovesAllEvents() {
        trail.record(AuditEvent.Action.LOG_INGESTED, "svc", "d", "c");
        trail.record(AuditEvent.Action.ALERT_FIRED, "svc", "d", "c");
        trail.clear();
        assertEquals(0, trail.size());
    }

    @Test
    void correlationIdNullWhenNotIncluded() {
        AuditConfig config = AuditConfig.builder().includeCorrelationId(false).build();
        LogAuditTrail noCorrelationTrail = new LogAuditTrail(config);
        noCorrelationTrail.record(AuditEvent.Action.LOG_INGESTED, "svc", "detail", "corr-99");
        assertNull(noCorrelationTrail.getEvents().get(0).getCorrelationId());
    }

    @Test
    void getEventsReturnsCopy() {
        trail.record(AuditEvent.Action.LOG_INGESTED, "svc", "d", "c");
        List<AuditEvent> snapshot = trail.getEvents();
        trail.record(AuditEvent.Action.LOG_DROPPED, "svc", "d2", "c2");
        assertEquals(1, snapshot.size());
        assertEquals(2, trail.size());
    }
}
