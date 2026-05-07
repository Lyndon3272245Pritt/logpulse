package com.logpulse.masking;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Defines a single masking rule that matches a regex pattern and replaces
 * sensitive data with a configured replacement string.
 */
public class MaskingRule {

    private final String name;
    private final Pattern pattern;
    private final String replacement;
    private final boolean enabled;

    public MaskingRule(String name, String regex, String replacement, boolean enabled) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(regex, "regex must not be null");
        Objects.requireNonNull(replacement, "replacement must not be null");
        this.name = name;
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Applies this rule to the given input, returning the masked result.
     *
     * @param input raw log message field
     * @return masked string, or original if rule is disabled
     */
    public String apply(String input) {
        if (!enabled || input == null) {
            return input;
        }
        return pattern.matcher(input).replaceAll(replacement);
    }

    @Override
    public String toString() {
        return "MaskingRule{name='" + name + "', pattern='" + pattern.pattern() +
                "', replacement='" + replacement + "', enabled=" + enabled + "}";
    }
}
