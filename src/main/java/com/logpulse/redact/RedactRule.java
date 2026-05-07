package com.logpulse.redact;

import java.util.regex.Pattern;

/**
 * Defines a single redaction rule: a named pattern and its replacement string.
 */
public class RedactRule {

    private final String name;
    private final Pattern pattern;
    private final String replacement;

    public RedactRule(String name, String regex, String replacement) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("RedactRule name must not be blank");
        }
        if (regex == null || regex.isBlank()) {
            throw new IllegalArgumentException("RedactRule regex must not be blank");
        }
        this.name = name;
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement != null ? replacement : "[REDACTED]";
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

    /**
     * Apply this rule to the given input, replacing all matches.
     *
     * @param input the text to process
     * @return text with all matches replaced
     */
    public String apply(String input) {
        if (input == null) {
            return null;
        }
        return pattern.matcher(input).replaceAll(replacement);
    }

    @Override
    public String toString() {
        return "RedactRule{name='" + name + "', pattern='" + pattern.pattern() +
               "', replacement='" + replacement + "'}";
    }
}
