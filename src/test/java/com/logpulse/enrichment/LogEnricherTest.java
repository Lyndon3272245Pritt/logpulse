package com.logpulse.enrichment;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogEnricherTest {

    private LogEntry sampleEntry;

    @BeforeEach
    void setUp() {
        Map<String, String> fields = new HashMap<>();
        fields.put("message", "test log message");
        fields.put("level", "INFO");
        sampleEntry = new LogEntry("auth-service", Instant.now(), "INFO", "test log message", fields);
    }

    @Test
    void enrichAddsEnvironmentField() {
        EnrichmentConfig config = EnrichmentConfig.builder()
                .addEnvironment(true)
                .environment("production")
                .build();
        LogEnricher enricher = new LogEnricher(config);

        LogEntry enriched = enricher.enrich(sampleEntry);

        assertNotNull(enriched);
        assertEquals("production", enriched.getFields().get("environment"));
    }

    @Test
    void enrichAddsServiceTagField() {
        EnrichmentConfig config = EnrichmentConfig.builder()
                .addServiceTag(true)
                .serviceTag("backend-v2")
                .build();
        LogEnricher enricher = new LogEnricher(config);

        LogEntry enriched = enricher.enrich(sampleEntry);

        assertEquals("backend-v2", enriched.getFields().get("service_tag"));
    }

    @Test
    void enrichAddsHostnameField() {
        EnrichmentConfig config = EnrichmentConfig.builder()
                .addHostname(true)
                .build();
        LogEnricher enricher = new LogEnricher(config);

        LogEntry enriched = enricher.enrich(sampleEntry);

        assertNotNull(enriched.getFields().get("hostname"));
        assertFalse(enriched.getFields().get("hostname").isBlank());
    }

    @Test
    void registerCustomEnricherAppendsField() {
        EnrichmentConfig config = EnrichmentConfig.builder().build();
        LogEnricher enricher = new LogEnricher(config);
        enricher.registerEnricher(entry -> Map.of("region", "us-east-1"));

        LogEntry enriched = enricher.enrich(sampleEntry);

        assertEquals("us-east-1", enriched.getFields().get("region"));
    }

    @Test
    void enrichReturnsNullForNullEntry() {
        EnrichmentConfig config = EnrichmentConfig.builder().build();
        LogEnricher enricher = new LogEnricher(config);

        assertNull(enricher.enrich(null));
    }

    @Test
    void failingCustomEnricherDoesNotBreakPipeline() {
        EnrichmentConfig config = EnrichmentConfig.builder()
                .addEnvironment(true)
                .environment("staging")
                .build();
        LogEnricher enricher = new LogEnricher(config);
        enricher.registerEnricher(entry -> { throw new RuntimeException("boom"); });
        enricher.registerEnricher(entry -> Map.of("safe", "yes"));

        LogEntry enriched = enricher.enrich(sampleEntry);

        assertEquals("staging", enriched.getFields().get("environment"));
        assertEquals("yes", enriched.getFields().get("safe"));
    }

    @Test
    void getEnricherCountReflectsBuiltinAndCustom() {
        EnrichmentConfig config = EnrichmentConfig.builder()
                .addHostname(true)
                .addEnvironment(true)
                .environment("dev")
                .build();
        LogEnricher enricher = new LogEnricher(config);
        enricher.registerEnricher(entry -> Map.of("custom", "value"));

        assertEquals(3, enricher.getEnricherCount());
    }
}
