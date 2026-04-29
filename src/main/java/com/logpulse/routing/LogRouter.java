package com.logpulse.routing;

import com.logpulse.filter.LogFilter;
import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Routes incoming log entries to registered consumers based on service name
 * and optional filter criteria. Supports wildcard routing via "*" service key.
 */
public class LogRouter {

    private final Map<String, List<RouteEntry>> routes = new ConcurrentHashMap<>();

    /**
     * Register a consumer for a specific service (or "*" for all services).
     *
     * @param serviceKey the service name to match, or "*" for wildcard
     * @param filter     optional filter; if null, all entries for the service are routed
     * @param consumer   the consumer to invoke with matched entries
     */
    public void register(String serviceKey, LogFilter filter, Consumer<LogEntry> consumer) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalArgumentException("serviceKey must not be null or blank");
        }
        if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        }
        routes.computeIfAbsent(serviceKey, k -> new ArrayList<>())
              .add(new RouteEntry(filter, consumer));
    }

    /**
     * Route a log entry to all matching registered consumers.
     *
     * @param entry the log entry to route
     */
    public void route(LogEntry entry) {
        if (entry == null) return;

        dispatch(entry, routes.getOrDefault(entry.getService(), List.of()));
        dispatch(entry, routes.getOrDefault("*", List.of()));
    }

    private void dispatch(LogEntry entry, List<RouteEntry> routeEntries) {
        for (RouteEntry re : routeEntries) {
            if (re.filter == null || re.filter.matches(entry)) {
                re.consumer.accept(entry);
            }
        }
    }

    /** Returns the number of registered route entries across all service keys. */
    public int routeCount() {
        return routes.values().stream().mapToInt(List::size).sum();
    }

    /** Removes all registered routes. */
    public void clear() {
        routes.clear();
    }

    private static class RouteEntry {
        final LogFilter filter;
        final Consumer<LogEntry> consumer;

        RouteEntry(LogFilter filter, Consumer<LogEntry> consumer) {
            this.filter = filter;
            this.consumer = consumer;
        }
    }
}
