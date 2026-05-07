package com.logpulse.audit;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class AuditConfigTest {

    @Test
    void defaultConfigHasAllActionsTracked() {
        AuditConfig config = AuditConfig.builder().build();
        assertTrue(config.isEnabled());
        assertEquals(10_000, config.getMaxRetainedEvents());
        assertEquals(EnumSet.allOf(AuditEvent.Action.class), config.getTrackedActions());
        assertTrue(config.isIncludeCorrelationId());
    }

    @Test
    void customConfigAppliesCorrectly() {
        AuditConfig config = AuditConfig.builder()
                .enabled(false)
                .maxRetainedEvents(500)
                .trackedActions(EnumSet.of(AuditEvent.Action.ALERT_FIRED, AuditEvent.Action.LOG_DROPPED))
                .includeCorrelationId(false)
                .build();

        assertFalse(config.isEnabled());
        assertEquals(500, config.getMaxRetainedEvents());
        assertTrue(config.getTrackedActions().contains(AuditEvent.Action.ALERT_FIRED));
        assertTrue(config.getTrackedActions().contains(AuditEvent.Action.LOG_DROPPED));
        assertFalse(config.getTrackedActions().contains(AuditEvent.Action.LOG_INGESTED));
        assertFalse(config.isIncludeCorrelationId());
    }

    @Test
    void invalidMaxRetainedEventsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                AuditConfig.builder().maxRetainedEvents(0).build());
        assertThrows(IllegalArgumentException.class, () ->
                AuditConfig.builder().maxRetainedEvents(-1).build());
    }

    @Test
    void emptyTrackedActionsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                AuditConfig.builder().trackedActions(EnumSet.noneOf(AuditEvent.Action.class)).build());
    }

    @Test
    void trackedActionsSetIsImmutable() {
        AuditConfig config = AuditConfig.builder().build();
        assertThrows(UnsupportedOperationException.class, () ->
                config.getTrackedActions().add(AuditEvent.Action.ALERT_FIRED));
    }
}
