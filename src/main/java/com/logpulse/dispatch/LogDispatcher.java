package com.logpulse.dispatch;

import com.logpulse.model.LogEntry;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Dispatches log entries to one or more registered targets according to
 * the configured dispatch strategy (BROADCAST, ROUND_ROBIN, FIRST_MATCH).
 */
public class LogDispatcher {

    private static final Logger LOGGER = Logger.getLogger(LogDispatcher.class.getName());

    private final DispatchConfig config;
    private final Map<String, DispatchTarget> targets = new LinkedHashMap<>();
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public LogDispatcher(DispatchConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public void registerTarget(DispatchTarget target) {
        Objects.requireNonNull(target);
        targets.put(target.getName(), target);
    }

    public void dispatch(LogEntry entry) {
        if (entry == null) return;
        List<DispatchTarget> ordered = resolveTargets();
        switch (config.getStrategy()) {
            case BROADCAST -> broadcast(entry, ordered);
            case ROUND_ROBIN -> roundRobin(entry, ordered);
            case FIRST_MATCH -> firstMatch(entry, ordered);
        }
    }

    private List<DispatchTarget> resolveTargets() {
        List<DispatchTarget> result = new ArrayList<>();
        for (String name : config.getTargetNames()) {
            DispatchTarget t = targets.get(name);
            if (t != null) result.add(t);
            else LOGGER.warning("Dispatch target not found: " + name);
        }
        return result;
    }

    private void broadcast(LogEntry entry, List<DispatchTarget> targets) {
        for (DispatchTarget t : targets) {
            if (t.matches(entry)) safeHandle(t, entry);
        }
    }

    private void roundRobin(LogEntry entry, List<DispatchTarget> targets) {
        if (targets.isEmpty()) return;
        int idx = Math.abs(roundRobinIndex.getAndIncrement() % targets.size());
        DispatchTarget t = targets.get(idx);
        if (t.matches(entry)) safeHandle(t, entry);
    }

    private void firstMatch(LogEntry entry, List<DispatchTarget> targets) {
        for (DispatchTarget t : targets) {
            if (t.matches(entry)) { safeHandle(t, entry); return; }
        }
    }

    private void safeHandle(DispatchTarget target, LogEntry entry) {
        int attempts = 0;
        int maxAttempts = config.getMaxRetries() + 1;
        while (attempts < maxAttempts) {
            try {
                target.handle(entry);
                return;
            } catch (Exception e) {
                attempts++;
                LOGGER.warning("Dispatch error on target '" + target.getName() + "' attempt " + attempts + ": " + e.getMessage());
                if (config.isFailFast() || attempts >= maxAttempts) throw new DispatchException(target.getName(), e);
            }
        }
    }

    public int getTargetCount() { return targets.size(); }
}
