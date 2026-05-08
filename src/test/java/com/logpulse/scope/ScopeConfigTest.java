package com.logpulse.scope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScopeConfigTest {

    @Test
    void buildsWithIncludedAndExcludedSources() {
        ScopeConfig config = ScopeConfig.builder("prod")
                .includeSource("auth-service")
                .includeSource("order-service")
                .excludeSource("debug-agent")
                .allowUnknownSources(false)
                .build();

        assertEquals("prod", config.getScopeName());
        assertTrue(config.getIncludedSources().contains("auth-service"));
        assertTrue(config.getIncludedSources().contains("order-service"));
        assertTrue(config.getExcludedSources().contains("debug-agent"));
        assertFalse(config.isAllowUnknownSources());
    }

    @Test
    void defaultsToDisallowUnknownSources() {
        ScopeConfig config = ScopeConfig.builder("staging").build();
        assertFalse(config.isAllowUnknownSources());
        assertTrue(config.getIncludedSources().isEmpty());
        assertTrue(config.getExcludedSources().isEmpty());
    }

    @Test
    void throwsOnBlankScopeName() {
        assertThrows(IllegalArgumentException.class, () -> ScopeConfig.builder("").build());
        assertThrows(IllegalArgumentException.class, () -> ScopeConfig.builder(null).build());
    }

    @Test
    void includedSourcesAreImmutable() {
        ScopeConfig config = ScopeConfig.builder("test")
                .includeSource("svc-a")
                .build();
        assertThrows(UnsupportedOperationException.class,
                () -> config.getIncludedSources().add("svc-b"));
    }

    @Test
    void allowUnknownSourcesCanBeEnabled() {
        ScopeConfig config = ScopeConfig.builder("open")
                .allowUnknownSources(true)
                .build();
        assertTrue(config.isAllowUnknownSources());
    }
}
