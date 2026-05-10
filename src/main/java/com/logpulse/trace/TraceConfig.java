package com.logpulse.trace;

import java.util.Objects;

/**
 * Configuration for distributed trace correlation in log entries.
 */
public class TraceConfig {

    private final String traceIdField;
    private final String spanIdField;
    private final String parentSpanIdField;
    private final boolean propagateContext;
    private final int maxTraceDepth;
    private final boolean dropOrphanedSpans;

    private TraceConfig(Builder builder) {
        this.traceIdField = builder.traceIdField;
        this.spanIdField = builder.spanIdField;
        this.parentSpanIdField = builder.parentSpanIdField;
        this.propagateContext = builder.propagateContext;
        this.maxTraceDepth = builder.maxTraceDepth;
        this.dropOrphanedSpans = builder.dropOrphanedSpans;
    }

    public String getTraceIdField() { return traceIdField; }
    public String getSpanIdField() { return spanIdField; }
    public String getParentSpanIdField() { return parentSpanIdField; }
    public boolean isPropagateContext() { return propagateContext; }
    public int getMaxTraceDepth() { return maxTraceDepth; }
    public boolean isDropOrphanedSpans() { return dropOrphanedSpans; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String traceIdField = "trace_id";
        private String spanIdField = "span_id";
        private String parentSpanIdField = "parent_span_id";
        private boolean propagateContext = true;
        private int maxTraceDepth = 50;
        private boolean dropOrphanedSpans = false;

        public Builder traceIdField(String traceIdField) {
            this.traceIdField = Objects.requireNonNull(traceIdField, "traceIdField must not be null");
            return this;
        }
        public Builder spanIdField(String spanIdField) {
            this.spanIdField = Objects.requireNonNull(spanIdField, "spanIdField must not be null");
            return this;
        }
        public Builder parentSpanIdField(String parentSpanIdField) {
            this.parentSpanIdField = Objects.requireNonNull(parentSpanIdField);
            return this;
        }
        public Builder propagateContext(boolean propagateContext) {
            this.propagateContext = propagateContext;
            return this;
        }
        public Builder maxTraceDepth(int maxTraceDepth) {
            if (maxTraceDepth < 1) throw new IllegalArgumentException("maxTraceDepth must be >= 1");
            this.maxTraceDepth = maxTraceDepth;
            return this;
        }
        public Builder dropOrphanedSpans(boolean dropOrphanedSpans) {
            this.dropOrphanedSpans = dropOrphanedSpans;
            return this;
        }
        public TraceConfig build() {
            return new TraceConfig(this);
        }
    }
}
