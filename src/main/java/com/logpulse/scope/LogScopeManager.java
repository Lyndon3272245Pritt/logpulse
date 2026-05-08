package com.logpulse.scope;

import com.logpulse.model.LogEntry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active scopes and evaluates whether a given {@link LogEntry}
 * falls within a registered scope based on its source.
 */
public class LogScopeManager {

    private final Map<String, ScopeConfig> scopes = new ConcurrentHashMap<>();
    private final Map<String, ScopeContext> contexts = new ConcurrentHashMap<>();

    public void registerScope(ScopeConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ScopeConfig must not be null");
        }
        scopes.put(config.getScopeName(), config);
        contexts.put(config.getScopeName(), new ScopeContext(config.getScopeName()));
    }

    public void unregisterScope(String scopeName) {
        scopes.remove(scopeName);
        contexts.remove(scopeName);
    }

    public boolean isInScope(String scopeName, LogEntry entry) {
        ScopeConfig config = scopes.get(scopeName);
        if (config == null || entry == null) {
            return false;
        }
        String source = entry.getSource();
        if (source == null) {
            return config.isAllowUnknownSources();
        }
        if (config.getExcludedSources().contains(source)) {
            return false;
        }
        if (!config.getIncludedSources().isEmpty()) {
            return config.getIncludedSources().contains(source);
        }
        return config.isAllowUnknownSources();
    }

    public Optional<ScopeContext> getContext(String scopeName) {
        return Optional.ofNullable(contexts.get(scopeName));
    }

    public boolean hasScope(String scopeName) {
        return scopes.containsKey(scopeName);
    }

    public int activeScopeCount() {
        return scopes.size();
    }

    public void clearAll() {
        scopes.clear();
        contexts.clear();
    }
}
