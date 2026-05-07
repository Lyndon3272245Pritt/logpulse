package com.logpulse.masking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration holder for the log masking feature.
 * Supports multiple {@link MaskingRule} instances and controls
 * whether masking is globally active.
 */
public class MaskingConfig {

    public static final String DEFAULT_REPLACEMENT = "[REDACTED]";

    private final boolean globallyEnabled;
    private final List<MaskingRule> rules;

    private MaskingConfig(Builder builder) {
        this.globallyEnabled = builder.globallyEnabled;
        this.rules = Collections.unmodifiableList(new ArrayList<>(builder.rules));
    }

    public boolean isGloballyEnabled() {
        return globallyEnabled;
    }

    public List<MaskingRule> getRules() {
        return rules;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean globallyEnabled = true;
        private final List<MaskingRule> rules = new ArrayList<>();

        public Builder globallyEnabled(boolean enabled) {
            this.globallyEnabled = enabled;
            return this;
        }

        public Builder addRule(MaskingRule rule) {
            if (rule != null) {
                rules.add(rule);
            }
            return this;
        }

        public Builder addRule(String name, String regex, String replacement) {
            return addRule(new MaskingRule(name, regex, replacement, true));
        }

        public Builder addRule(String name, String regex) {
            return addRule(name, regex, DEFAULT_REPLACEMENT);
        }

        public MaskingConfig build() {
            return new MaskingConfig(this);
        }
    }
}
