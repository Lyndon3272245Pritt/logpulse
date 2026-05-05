package com.logpulse.sampling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SamplerConfigTest {

    @Test
    void defaultSampleRateIsOne() {
        SamplerConfig config = SamplerConfig.builder().build();
        assertEquals(1, config.getSampleRate());
    }

    @Test
    void customSampleRateIsStored() {
        SamplerConfig config = SamplerConfig.builder().sampleRate(10).build();
        assertEquals(10, config.getSampleRate());
    }

    @Test
    void invalidSampleRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SamplerConfig.builder().sampleRate(0).build());
        assertThrows(IllegalArgumentException.class,
                () -> SamplerConfig.builder().sampleRate(-5).build());
    }

    @Test
    void forcedLevelIsRecognised() {
        SamplerConfig config = SamplerConfig.builder()
                .forceLevel(SamplerConfig.Level.ERROR)
                .forceLevel(SamplerConfig.Level.FATAL)
                .build();
        assertTrue(config.isForcedLevel("ERROR"));
        assertTrue(config.isForcedLevel("error"));
        assertTrue(config.isForcedLevel("FATAL"));
        assertFalse(config.isForcedLevel("WARN"));
    }

    @Test
    void nullOrBlankLevelReturnsFalse() {
        SamplerConfig config = SamplerConfig.builder().build();
        assertFalse(config.isForcedLevel(null));
        assertFalse(config.isForcedLevel(""));
        assertFalse(config.isForcedLevel("  "));
    }

    @Test
    void unknownLevelStringReturnsFalse() {
        SamplerConfig config = SamplerConfig.builder()
                .forceLevel(SamplerConfig.Level.ERROR)
                .build();
        assertFalse(config.isForcedLevel("CRITICAL"));
    }

    @Test
    void forcedLevelsSetIsImmutable() {
        SamplerConfig config = SamplerConfig.builder()
                .forceLevel(SamplerConfig.Level.WARN)
                .build();
        assertThrows(UnsupportedOperationException.class,
                () -> config.getForcedLevels().add(SamplerConfig.Level.DEBUG));
    }
}
