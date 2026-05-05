package com.logpulse.sampling;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogSamplerTest {

    private LogEntry makeEntry(String level) {
        LogEntry entry = mock(LogEntry.class);
        when(entry.getLevel()).thenReturn(level);
        return entry;
    }

    @Test
    void nullConfigThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LogSampler(null));
    }

    @Test
    void nullEntryReturnsFalse() {
        LogSampler sampler = new LogSampler(SamplerConfig.builder().build());
        assertFalse(sampler.sample(null));
    }

    @Test
    void rateOneAcceptsAll() {
        LogSampler sampler = new LogSampler(SamplerConfig.builder().sampleRate(1).build());
        for (int i = 0; i < 20; i++) {
            assertTrue(sampler.sample(makeEntry("INFO")));
        }
        assertEquals(20, sampler.getTotalSeen());
        assertEquals(20, sampler.getTotalAccepted());
    }

    @Test
    void rateThreeAcceptsEveryThird() {
        LogSampler sampler = new LogSampler(SamplerConfig.builder().sampleRate(3).build());
        int accepted = 0;
        for (int i = 0; i < 9; i++) {
            if (sampler.sample(makeEntry("DEBUG"))) accepted++;
        }
        assertEquals(3, accepted);
        assertEquals(9, sampler.getTotalSeen());
    }

    @Test
    void forcedLevelAlwaysPasses() {
        SamplerConfig config = SamplerConfig.builder()
                .sampleRate(100)
                .forceLevel(SamplerConfig.Level.ERROR)
                .build();
        LogSampler sampler = new LogSampler(config);
        for (int i = 0; i < 10; i++) {
            assertTrue(sampler.sample(makeEntry("ERROR")));
        }
        assertEquals(10, sampler.getTotalAccepted());
    }

    @Test
    void forcedLevelDoesNotAffectOtherLevels() {
        SamplerConfig config = SamplerConfig.builder()
                .sampleRate(10)
                .forceLevel(SamplerConfig.Level.FATAL)
                .build();
        LogSampler sampler = new LogSampler(config);
        // 9 INFO entries — none should pass (10th would, but we send only 9)
        for (int i = 0; i < 9; i++) {
            assertFalse(sampler.sample(makeEntry("INFO")));
        }
    }

    @Test
    void acceptanceRatioIsCorrect() {
        LogSampler sampler = new LogSampler(SamplerConfig.builder().sampleRate(4).build());
        for (int i = 0; i < 8; i++) sampler.sample(makeEntry("INFO"));
        assertEquals(0.25, sampler.getAcceptanceRatio(), 1e-9);
    }

    @Test
    void resetClearsCounters() {
        LogSampler sampler = new LogSampler(SamplerConfig.builder().sampleRate(1).build());
        sampler.sample(makeEntry("INFO"));
        sampler.reset();
        assertEquals(0, sampler.getTotalSeen());
        assertEquals(0, sampler.getTotalAccepted());
        assertEquals(0.0, sampler.getAcceptanceRatio());
    }
}
