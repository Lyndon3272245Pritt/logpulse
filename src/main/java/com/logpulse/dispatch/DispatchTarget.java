package com.logpulse.dispatch;

import com.logpulse.model.LogEntry;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a named dispatch target with an optional filter predicate
 * and a handler consumer for log entries.
 */
public class DispatchTarget {

    private final String name;
    private final Predicate<LogEntry> predicate;
    private final Consumer<LogEntry> handler;

    public DispatchTarget(String name, Predicate<LogEntry> predicate, Consumer<LogEntry> handler) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.predicate = predicate != null ? predicate : entry -> true;
        this.handler = Objects.requireNonNull(handler, "handler must not be null");
    }

    public DispatchTarget(String name, Consumer<LogEntry> handler) {
        this(name, null, handler);
    }

    public String getName() { return name; }

    public boolean matches(LogEntry entry) {
        return predicate.test(entry);
    }

    public void handle(LogEntry entry) {
        handler.accept(entry);
    }

    @Override
    public String toString() {
        return "DispatchTarget{name='" + name + "'}";
    }
}
