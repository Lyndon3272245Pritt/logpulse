package com.logpulse.enrichment;

/**
 * Configuration for the LogEnricher, controlling which built-in enrichment
 * strategies are enabled and their associated parameters.
 */
public class EnrichmentConfig {

    private boolean addHostname;
    private boolean addEnvironment;
    private String environment;
    private boolean addServiceTag;
    private String serviceTag;

    private EnrichmentConfig() {}

    public static Builder builder() {
        return new Builder();
    }

    public boolean isAddHostname() {
        return addHostname;
    }

    public boolean isAddEnvironment() {
        return addEnvironment;
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isAddServiceTag() {
        return addServiceTag;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public static class Builder {
        private final EnrichmentConfig config = new EnrichmentConfig();

        public Builder addHostname(boolean addHostname) {
            config.addHostname = addHostname;
            return this;
        }

        public Builder addEnvironment(boolean addEnvironment) {
            config.addEnvironment = addEnvironment;
            return this;
        }

        public Builder environment(String environment) {
            config.environment = environment;
            return this;
        }

        public Builder addServiceTag(boolean addServiceTag) {
            config.addServiceTag = addServiceTag;
            return this;
        }

        public Builder serviceTag(String serviceTag) {
            config.serviceTag = serviceTag;
            return this;
        }

        public EnrichmentConfig build() {
            return config;
        }
    }
}
