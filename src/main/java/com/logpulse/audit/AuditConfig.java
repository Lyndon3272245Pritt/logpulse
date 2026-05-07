package com.logpulse.audit;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration for the audit trail subsystem.
 */
public class AuditConfig {

    private final boolean enabled;
    private final int maxRetainedEvents;
    private final Set<AuditEvent.Action> trackedActions;
    private final boolean includeCorrelationId;

    private AuditConfig(Builder builder) {
        this.enabled = builder.enabled;
        this.maxRetainedEvents = builder.maxRetainedEvents;
        this.trackedActions = Collections.unmodifiableSet(
                EnumSet.copyOf(builder.trackedActions));
        this.includeCorrelationId = builder.includeCorrelationId;
    }

    public boolean isEnabled() { return enabled; }
    public int getMaxRetainedEvents() { return maxRetainedEvents; }
    public Set<AuditEvent.Action> getTrackedActions() { return trackedActions; }
    public boolean isIncludeCorrelationId() { return includeCorrelationId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean enabled = true;
        private int maxRetainedEvents = 10_000;
        private Set<AuditEvent.Action> trackedActions =
                EnumSet.allOf(AuditEvent.Action.class);
        private boolean includeCorrelationId = true;

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder maxRetainedEvents(int max) {
            if (max <= 0) throw new IllegalArgumentException("maxRetainedEvents must be positive");
            this.maxRetainedEvents = max;
            return this;
        }

        public Builder trackedActions(Set<AuditEvent.Action> actions) {
            if (actions == null || actions.isEmpty())
                throw new IllegalArgumentException("trackedActions must not be empty");
            this.trackedActions = EnumSet.copyOf(actions);
            return this;
        }

        public Builder includeCorrelationId(boolean include) {
            this.includeCorrelationId = include;
            return this;
        }

        public AuditConfig build() { return new AuditConfig(this); }
    }
}
