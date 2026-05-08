package com.logpulse.scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds runtime metadata associated with a processing scope,
 * such as active source labels and user-defined attributes.
 */
public class ScopeContext {

    private final String scopeName;
    private final Map<String, String> attributes;

    public ScopeContext(String scopeName) {
        if (scopeName == null || scopeName.isBlank()) {
            throw new IllegalArgumentException("Scope name must not be blank");
        }
        this.scopeName = scopeName;
        this.attributes = new HashMap<>();
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setAttribute(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Attribute key must not be blank");
        }
        attributes.put(key, value);
    }

    public Optional<String> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void clear() {
        attributes.clear();
    }

    @Override
    public String toString() {
        return "ScopeContext{scope='" + scopeName + "', attributes=" + attributes + "}";
    }
}
