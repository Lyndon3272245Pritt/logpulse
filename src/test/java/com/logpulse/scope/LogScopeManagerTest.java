package com.logpulse.scope;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LogScopeManagerTest {

    private LogScopeManager manager;

    @BeforeEach
    void setUp() {
        manager = new LogScopeManager();
    }

    private LogEntry entryFromSource(String source) {
        return new LogEntry(source, "INFO", "test message", Instant.now());
    }

    @Test
    void registersAndDetectsScope() {
        ScopeConfig config = ScopeConfig.builder("prod")
                .includeSource("api-gateway")
                .build();
        manager.registerScope(config);
        assertTrue(manager.hasScope("prod"));
        assertEquals(1, manager.activeScopeCount());
    }

    @Test
    void entryInIncludedSourceIsInScope() {
        ScopeConfig config = ScopeConfig.builder("prod")
                .includeSource("api-gateway")
                .build();
        manager.registerScope(config);
        assertTrue(manager.isInScope("prod", entryFromSource("api-gateway")));
    }

    @Test
    void entryNotInIncludedSourceIsOutOfScope() {
        ScopeConfig config = ScopeConfig.builder("prod")
                .includeSource("api-gateway")
                .build();
        manager.registerScope(config);
        assertFalse(manager.isInScope("prod", entryFromSource("batch-worker")));
    }

    @Test
    void excludedSourceIsAlwaysOutOfScope() {
        ScopeConfig config = ScopeConfig.builder("prod")
                .allowUnknownSources(true)
                .excludeSource("noise-service")
                .build();
        manager.registerScope(config);
        assertFalse(manager.isInScope("prod", entryFromSource("noise-service")));
    }

    @Test
    void unknownSourceAllowedWhenConfigured() {
        ScopeConfig config = ScopeConfig.builder("open")
                .allowUnknownSources(true)
                .build();
        manager.registerScope(config);
        assertTrue(manager.isInScope("open", entryFromSource("any-service")));
    }

    @Test
    void unregisteredScopeReturnsFalse() {
        assertFalse(manager.isInScope("ghost", entryFromSource("svc")));
    }

    @Test
    void contextIsAvailableAfterRegistration() {
        ScopeConfig config = ScopeConfig.builder("ctx-scope").build();
        manager.registerScope(config);
        Optional<ScopeContext> ctx = manager.getContext("ctx-scope");
        assertTrue(ctx.isPresent());
        assertEquals("ctx-scope", ctx.get().getScopeName());
    }

    @Test
    void clearAllRemovesAllScopes() {
        manager.registerScope(ScopeConfig.builder("s1").build());
        manager.registerScope(ScopeConfig.builder("s2").build());
        manager.clearAll();
        assertEquals(0, manager.activeScopeCount());
        assertTrue(manager.getContext("s1").isEmpty());
    }

    @Test
    void unregisterRemovesSingleScope() {
        manager.registerScope(ScopeConfig.builder("to-remove").build());
        manager.registerScope(ScopeConfig.builder("to-keep").build());
        manager.unregisterScope("to-remove");
        assertFalse(manager.hasScope("to-remove"));
        assertTrue(manager.hasScope("to-keep"));
    }
}
