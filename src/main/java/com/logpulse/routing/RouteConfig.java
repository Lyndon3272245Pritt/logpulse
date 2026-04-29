package com.logpulse.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable configuration describing a single routing rule.
 * Constructed via the inner {@link Builder}.
 */
public final class RouteConfig {

    private final String serviceKey;
    private final List<String> levels;
    private final String messageContains;

    private RouteConfig(Builder builder) {
        this.serviceKey = builder.serviceKey;
        this.levels = Collections.unmodifiableList(new ArrayList<>(builder.levels));
        this.messageContains = builder.messageContains;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public List<String> getLevels() {
        return levels;
    }

    public String getMessageContains() {
        return messageContains;
    }

    @Override
    public String toString() {
        return "RouteConfig{service='" + serviceKey +
               "', levels=" + levels +
               ", messageContains='" + messageContains + "'}";
    }

    public static Builder builder(String serviceKey) {
        return new Builder(serviceKey);
    }

    public static final class Builder {
        private final String serviceKey;
        private final List<String> levels = new ArrayList<>();
        private String messageContains;

        private Builder(String serviceKey) {
            if (serviceKey == null || serviceKey.isBlank()) {
                throw new IllegalArgumentException("serviceKey must not be blank");
            }
            this.serviceKey = serviceKey;
        }

        public Builder withLevel(String level) {
            if (level != null && !level.isBlank()) {
                this.levels.add(level.toUpperCase());
            }
            return this;
        }

        public Builder withMessageContains(String substring) {
            this.messageContains = substring;
            return this;
        }

        public RouteConfig build() {
            return new RouteConfig(this);
        }
    }
}
