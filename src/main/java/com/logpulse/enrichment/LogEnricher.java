package com.logpulse.enrichment;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Enriches log entries with additional metadata fields based on configured enrichment rules.
 * Enrichers are applied in order and can add or override fields on the log entry.
 */
public class LogEnricher {

    private final EnrichmentConfig config;
    private final List<Function<LogEntry, Map<String, String>>> enrichers;

    public LogEnricher(EnrichmentConfig config) {
        this.config = config;
        this.enrichers = new ArrayList<>();
        initBuiltinEnrichers();
    }

    private void initBuiltinEnrichers() {
        if (config.isAddHostname()) {
            enrichers.add(entry -> Map.of("hostname", resolveHostname()));
        }
        if (config.isAddEnvironment() && config.getEnvironment() != null) {
            String env = config.getEnvironment();
            enrichers.add(entry -> Map.of("environment", env));
        }
        if (config.isAddServiceTag() && config.getServiceTag() != null) {
            String tag = config.getServiceTag();
            enrichers.add(entry -> Map.of("service_tag", tag));
        }
    }

    /**
     * Registers a custom enrichment function that produces additional fields.
     *
     * @param enricher function that takes a LogEntry and returns a map of fields to add
     */
    public void registerEnricher(Function<LogEntry, Map<String, String>> enricher) {
        if (enricher != null) {
            enrichers.add(enricher);
        }
    }

    /**
     * Applies all registered enrichers to the given log entry.
     *
     * @param entry the log entry to enrich
     * @return a new LogEntry with additional fields merged in
     */
    public LogEntry enrich(LogEntry entry) {
        if (entry == null) {
            return null;
        }
        Map<String, String> fields = entry.getFields();
        for (Function<LogEntry, Map<String, String>> enricher : enrichers) {
            try {
                Map<String, String> additions = enricher.apply(entry);
                if (additions != null) {
                    fields.putAll(additions);
                }
            } catch (Exception e) {
                // skip failed enricher, log entry remains partially enriched
            }
        }
        return entry;
    }

    public int getEnricherCount() {
        return enrichers.size();
    }

    private String resolveHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}
