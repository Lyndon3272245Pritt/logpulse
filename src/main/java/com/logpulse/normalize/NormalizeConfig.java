package com.logpulse.normalize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for log field normalization rules.
 * Supports field renaming, value coercion, and default injection.
 */
public class NormalizeConfig {

    private final Map<String, String> fieldRenames;
    private final Map<String, String> fieldDefaults;
    private final boolean lowercaseLevel;
    private final boolean trimWhitespace;

    private NormalizeConfig(Builder builder) {
        this.fieldRenames = Collections.unmodifiableMap(new HashMap<>(builder.fieldRenames));
        this.fieldDefaults = Collections.unmodifiableMap(new HashMap<>(builder.fieldDefaults));
        this.lowercaseLevel = builder.lowercaseLevel;
        this.trimWhitespace = builder.trimWhitespace;
    }

    public Map<String, String> getFieldRenames() {
        return fieldRenames;
    }

    public Map<String, String> getFieldDefaults() {
        return fieldDefaults;
    }

    public boolean isLowercaseLevel() {
        return lowercaseLevel;
    }

    public boolean isTrimWhitespace() {
        return trimWhitespace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> fieldRenames = new HashMap<>();
        private final Map<String, String> fieldDefaults = new HashMap<>();
        private boolean lowercaseLevel = true;
        private boolean trimWhitespace = true;

        public Builder renameField(String from, String to) {
            if (from == null || from.isBlank()) throw new IllegalArgumentException("Source field name must not be blank");
            if (to == null || to.isBlank()) throw new IllegalArgumentException("Target field name must not be blank");
            fieldRenames.put(from, to);
            return this;
        }

        public Builder defaultField(String field, String value) {
            if (field == null || field.isBlank()) throw new IllegalArgumentException("Field name must not be blank");
            fieldDefaults.put(field, value == null ? "" : value);
            return this;
        }

        public Builder lowercaseLevel(boolean lowercase) {
            this.lowercaseLevel = lowercase;
            return this;
        }

        public Builder trimWhitespace(boolean trim) {
            this.trimWhitespace = trim;
            return this;
        }

        public NormalizeConfig build() {
            return new NormalizeConfig(this);
        }
    }
}
