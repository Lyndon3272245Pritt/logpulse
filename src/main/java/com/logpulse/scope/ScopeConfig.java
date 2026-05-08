package com.logpulse.scope;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for log scoping — restricts processing to a named set of services or sources.
 */
public class ScopeConfig {

    private final String scopeName;
    private final Set<String> includedSources;
    private final Set<String> excludedSources;
    private final boolean allowUnknownSources;

    private ScopeConfig(Builder builder) {
        this.scopeName = builder.scopeName;
        this.includedSources = Collections.unmodifiableSet(new HashSet<>(builder.includedSources));
        this.excludedSources = Collections.unmodifiableSet(new HashSet<>(builder.excludedSources));
        this.allowUnknownSources = builder.allowUnknownSources;
    }

    public String getScopeName() {
        return scopeName;
    }

    public Set<String> getIncludedSources() {
        return includedSources;
    }

    public Set<String> getExcludedSources() {
        return excludedSources;
    }

    public boolean isAllowUnknownSources() {
        return allowUnknownSources;
    }

    public static Builder builder(String scopeName) {
        return new Builder(scopeName);
    }

    public static class Builder {
        private final String scopeName;
        private final Set<String> includedSources = new HashSet<>();
        private final Set<String> excludedSources = new HashSet<>();
        private boolean allowUnknownSources = false;

        private Builder(String scopeName) {
            if (scopeName == null || scopeName.isBlank()) {
                throw new IllegalArgumentException("Scope name must not be blank");
            }
            this.scopeName = scopeName;
        }

        public Builder includeSource(String source) {
            this.includedSources.add(source);
            return this;
        }

        public Builder excludeSource(String source) {
            this.excludedSources.add(source);
            return this;
        }

        public Builder allowUnknownSources(boolean allow) {
            this.allowUnknownSources = allow;
            return this;
        }

        public ScopeConfig build() {
            return new ScopeConfig(this);
        }
    }
}
