package com.logpulse.pipeline;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LogPipelineTest {

    private LogEntry sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = new LogEntry("auth-service", "ERROR", "Login failed", Instant.now());
    }

    private PipelineConfig singleStageConfig() {
        return PipelineConfig.builder("test-pipeline")
                .addStage("passthrough")
                .build();
    }

    @Test
    void passthrough_stage_returns_entry_unchanged() {
        PipelineStage passthrough = Optional::ofNullable;
        LogPipeline pipeline = new LogPipeline(singleStageConfig(), List.of(passthrough));

        Optional<LogEntry> result = pipeline.process(sampleEntry);

        assertTrue(result.isPresent());
        assertSame(sampleEntry, result.get());
    }

    @Test
    void dropping_stage_returns_empty() {
        PipelineStage dropper = entry -> Optional.empty();
        LogPipeline pipeline = new LogPipeline(singleStageConfig(), List.of(dropper));

        Optional<LogEntry> result = pipeline.process(sampleEntry);

        assertTrue(result.isEmpty());
    }

    @Test
    void stages_are_applied_in_order() {
        StringBuilder order = new StringBuilder();
        PipelineStage first  = e -> { order.append("A"); return Optional.of(e); };
        PipelineStage second = e -> { order.append("B"); return Optional.of(e); };

        LogPipeline pipeline = new LogPipeline(singleStageConfig(), List.of(first, second));
        pipeline.process(sampleEntry);

        assertEquals("AB", order.toString());
    }

    @Test
    void stop_on_error_true_drops_entry_when_stage_throws() {
        PipelineStage boom = e -> { throw new RuntimeException("boom"); };
        PipelineConfig cfg = PipelineConfig.builder("err-pipeline")
                .addStage("s1")
                .stopOnError(true)
                .build();
        LogPipeline pipeline = new LogPipeline(cfg, List.of(boom));

        Optional<LogEntry> result = pipeline.process(sampleEntry);

        assertTrue(result.isEmpty());
    }

    @Test
    void stop_on_error_false_continues_after_exception() {
        PipelineStage boom = e -> { throw new RuntimeException("boom"); };
        PipelineStage passthrough = Optional::ofNullable;
        PipelineConfig cfg = PipelineConfig.builder("err-pipeline")
                .addStage("s1")
                .stopOnError(false)
                .build();
        LogPipeline pipeline = new LogPipeline(cfg, List.of(boom, passthrough));

        Optional<LogEntry> result = pipeline.process(sampleEntry);

        assertTrue(result.isPresent());
    }

    @Test
    void process_batch_filters_dropped_entries() {
        int[] count = {0};
        PipelineStage everyOther = e -> (++count[0] % 2 == 0) ? Optional.of(e) : Optional.empty();
        LogPipeline pipeline = new LogPipeline(singleStageConfig(), List.of(everyOther));

        List<LogEntry> batch = List.of(sampleEntry, sampleEntry, sampleEntry, sampleEntry);
        List<LogEntry> results = pipeline.processBatch(batch);

        assertEquals(2, results.size());
    }

    @Test
    void constructor_rejects_empty_stage_list() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogPipeline(singleStageConfig(), List.of()));
    }
}
