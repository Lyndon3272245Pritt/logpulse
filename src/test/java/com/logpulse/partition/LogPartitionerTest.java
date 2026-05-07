package com.logpulse.partition;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogPartitionerTest {

    private LogEntry entryA;
    private LogEntry entryB;
    private LogEntry entryC;

    @BeforeEach
    void setUp() {
        entryA = new LogEntry(Instant.now(), "auth-service", "ERROR", "Login failed", Map.of());
        entryB = new LogEntry(Instant.now(), "payment-service", "WARN", "Slow response", Map.of());
        entryC = new LogEntry(Instant.now(), "auth-service", "INFO", "User logged in", Map.of());
    }

    @Test
    void testPartitionByService() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.SERVICE)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.partition(entryB);
        partitioner.partition(entryC);

        assertTrue(partitioner.getPartitionNames().contains("auth-service"));
        assertTrue(partitioner.getPartitionNames().contains("payment-service"));
        assertEquals(2, partitioner.getPartition("auth-service").size());
        assertEquals(1, partitioner.getPartition("payment-service").size());
    }

    @Test
    void testPartitionByLevel() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.LEVEL)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.partition(entryB);
        partitioner.partition(entryC);

        assertTrue(partitioner.getPartitionNames().contains("ERROR"));
        assertTrue(partitioner.getPartitionNames().contains("WARN"));
        assertTrue(partitioner.getPartitionNames().contains("INFO"));
    }

    @Test
    void testCustomKeyExtractor() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.CUSTOM)
                .customKeyExtractor(entry -> entry.getMessage().contains("failed") ? "errors" : "other")
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.partition(entryB);

        assertEquals(1, partitioner.getPartition("errors").size());
        assertEquals(1, partitioner.getPartition("other").size());
    }

    @Test
    void testPartitionCountsAreTracked() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.SERVICE)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.partition(entryC);

        Map<String, Integer> counts = partitioner.getPartitionCounts();
        assertEquals(2, counts.get("auth-service"));
    }

    @Test
    void testClearPartition() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.SERVICE)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.clearPartition("auth-service");

        assertTrue(partitioner.getPartition("auth-service").isEmpty());
    }

    @Test
    void testClearAll() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.SERVICE)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.partition(entryB);
        partitioner.clearAll();

        assertTrue(partitioner.getPartitionNames().isEmpty());
    }

    @Test
    void testNullEntryThrows() {
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.SERVICE)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);
        assertThrows(IllegalArgumentException.class, () -> partitioner.partition(null));
    }

    @Test
    void testNamedPartitionsBucketByHash() {
        List<String> names = Arrays.asList("bucket-0", "bucket-1", "bucket-2");
        PartitionConfig config = PartitionConfig.builder()
                .strategy(PartitionStrategy.SERVICE)
                .partitionNames(names)
                .build();
        LogPartitioner partitioner = new LogPartitioner(config);

        partitioner.partition(entryA);
        partitioner.partition(entryB);

        long totalEntries = partitioner.getPartitionCounts().values().stream().mapToInt(i -> i).sum();
        assertEquals(2, totalEntries);
        partitioner.getPartitionNames().forEach(name -> assertTrue(names.contains(name)));
    }
}
