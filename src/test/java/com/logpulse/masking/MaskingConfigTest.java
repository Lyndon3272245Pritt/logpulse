package com.logpulse.masking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskingConfigTest {

    @Test
    void defaultsAreApplied() {
        MaskingConfig config = MaskingConfig.builder().build();
        assertTrue(config.isGloballyEnabled());
        assertTrue(config.getRules().isEmpty());
    }

    @Test
    void canDisableGlobally() {
        MaskingConfig config = MaskingConfig.builder()
                .globallyEnabled(false)
                .build();
        assertFalse(config.isGloballyEnabled());
    }

    @Test
    void rulesAreAddedCorrectly() {
        MaskingConfig config = MaskingConfig.builder()
                .addRule("email", "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")
                .addRule("token", "Bearer\\s+\\S+", "Bearer [REDACTED]")
                .build();

        assertEquals(2, config.getRules().size());
        assertEquals("email", config.getRules().get(0).getName());
        assertEquals("token", config.getRules().get(1).getName());
    }

    @Test
    void rulesListIsImmutable() {
        MaskingConfig config = MaskingConfig.builder()
                .addRule("ip", "\\d{1,3}(\\.\\d{1,3}){3}")
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> config.getRules().add(new MaskingRule("x", "x", "y", true)));
    }

    @Test
    void nullRuleIsIgnored() {
        MaskingConfig config = MaskingConfig.builder()
                .addRule(null)
                .build();
        assertTrue(config.getRules().isEmpty());
    }

    @Test
    void defaultReplacementConstant() {
        assertEquals("[REDACTED]", MaskingConfig.DEFAULT_REPLACEMENT);
    }
}
