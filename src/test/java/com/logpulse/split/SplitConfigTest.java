package com.logpulse.split;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitConfigTest {

    @Test
    void defaultValues() {
        SplitConfig cfg = new SplitConfig();
        assertEquals(SplitConfig.SplitStrategy.FIELD_VALUE, cfg.getStrategy());
        assertEquals("service", cfg.getSplitField());
        assertEquals(64, cfg.getMaxBuckets());
        assertFalse(cfg.isDropUnmatched());
        assertEquals("__unmatched__", cfg.getUnmatchedBucket());
        assertTrue(cfg.getAllowedBuckets().isEmpty());
    }

    @Test
    void fluentSetters() {
        SplitConfig cfg = new SplitConfig()
                .setStrategy(SplitConfig.SplitStrategy.PATTERN)
                .setSplitPattern("(ERROR|WARN)")
                .setMaxBuckets(10)
                .setDropUnmatched(true)
                .setUnmatchedBucket("other")
                .setAllowedBuckets(List.of("ERROR", "WARN"));

        assertEquals(SplitConfig.SplitStrategy.PATTERN, cfg.getStrategy());
        assertEquals("(ERROR|WARN)", cfg.getSplitPattern());
        assertEquals(10, cfg.getMaxBuckets());
        assertTrue(cfg.isDropUnmatched());
        assertEquals("other", cfg.getUnmatchedBucket());
        assertEquals(2, cfg.getAllowedBuckets().size());
    }

    @Test
    void maxBucketsValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new SplitConfig().setMaxBuckets(0));
    }

    @Test
    void nullStrategyThrows() {
        assertThrows(NullPointerException.class,
                () -> new SplitConfig().setStrategy(null));
    }

    @Test
    void toStringContainsStrategy() {
        String s = new SplitConfig().toString();
        assertTrue(s.contains("FIELD_VALUE"));
    }
}
