package com.logpulse.tag;

import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LogTaggerTest {

    private LogEntry baseEntry;

    @BeforeEach
    void setUp() {
        baseEntry = LogEntry.builder()
                .timestamp(Instant.now())
                .level("ERROR")
                .service("payment-service")
                .message("Connection timeout occurred")
                .fields(Map.of("env", "production"))
                .tags(new ArrayList<>())
                .build();
    }

    @Test
    void shouldApplyTagWhenLevelMatches() {
        TagRule rule = new TagRule("error-alert", "level", null, "ERROR");
        TagConfig config = new TagConfig(List.of(rule));
        LogTagger tagger = new LogTagger(config);

        LogEntry tagged = tagger.tag(baseEntry);

        assertTrue(tagged.getTags().contains("error-alert"));
    }

    @Test
    void shouldApplyTagWhenMessageMatchesPattern() {
        TagRule rule = new TagRule("timeout", "message", "(?i)timeout", null);
        TagConfig config = new TagConfig(List.of(rule));
        LogTagger tagger = new LogTagger(config);

        LogEntry tagged = tagger.tag(baseEntry);

        assertTrue(tagged.getTags().contains("timeout"));
    }

    @Test
    void shouldNotApplyTagWhenNoRuleMatches() {
        TagRule rule = new TagRule("debug-tag", "level", null, "DEBUG");
        TagConfig config = new TagConfig(List.of(rule));
        LogTagger tagger = new LogTagger(config);

        LogEntry tagged = tagger.tag(baseEntry);

        assertFalse(tagged.getTags().contains("debug-tag"));
    }

    @Test
    void shouldApplyMultipleTagsFromMultipleRules() {
        List<TagRule> rules = List.of(
                new TagRule("error-alert", "level", null, "ERROR"),
                new TagRule("prod", "env", null, "production"),
                new TagRule("payment", "service", "payment.*", null)
        );
        TagConfig config = new TagConfig(rules);
        LogTagger tagger = new LogTagger(config);

        LogEntry tagged = tagger.tag(baseEntry);

        assertTrue(tagged.getTags().containsAll(List.of("error-alert", "prod", "payment")));
    }

    @Test
    void shouldPreserveExistingTagsWhenAddingNew() {
        LogEntry entryWithTag = baseEntry.withTags(new ArrayList<>(List.of("existing-tag")));
        TagRule rule = new TagRule("error-alert", "level", null, "ERROR");
        TagConfig config = new TagConfig(List.of(rule));
        LogTagger tagger = new LogTagger(config);

        LogEntry tagged = tagger.tag(entryWithTag);

        assertTrue(tagged.getTags().contains("existing-tag"));
        assertTrue(tagged.getTags().contains("error-alert"));
    }

    @Test
    void shouldReturnNullForNullEntry() {
        TagConfig config = new TagConfig(List.of());
        LogTagger tagger = new LogTagger(config);

        assertNull(tagger.tag(null));
    }

    @Test
    void shouldTagAllEntriesInBatch() {
        TagRule rule = new TagRule("error-alert", "level", null, "ERROR");
        TagConfig config = new TagConfig(List.of(rule));
        LogTagger tagger = new LogTagger(config);

        LogEntry second = LogEntry.builder()
                .timestamp(Instant.now())
                .level("INFO")
                .service("auth-service")
                .message("User logged in")
                .tags(new ArrayList<>())
                .build();

        List<LogEntry> tagged = tagger.tagAll(List.of(baseEntry, second));

        assertEquals(2, tagged.size());
        assertTrue(tagged.get(0).getTags().contains("error-alert"));
        assertFalse(tagged.get(1).getTags().contains("error-alert"));
    }

    @Test
    void shouldThrowWhenConfigIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new LogTagger(null));
    }
}
