package com.logpulse.fanout;

import com.logpulse.model.LogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Fans out each incoming LogEntry to multiple named subscribers.
 * Subscribers can be added or removed at runtime.
 */
public class LogFanout {

    private final FanoutConfig config;
    private final Map<String, Consumer<LogEntry>> subscribers = new ConcurrentHashMap<>();
    private final List<FanoutErrorListener> errorListeners = Collections.synchronizedList(new ArrayList<>());

    public LogFanout(FanoutConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("FanoutConfig must not be null");
        }
        this.config = config;
    }

    /** Register a named subscriber. Replaces any existing subscriber with the same name. */
    public void subscribe(String name, Consumer<LogEntry> handler) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subscriber name must not be blank");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        }
        subscribers.put(name, handler);
    }

    /** Remove a subscriber by name. Returns true if the subscriber existed. */
    public boolean unsubscribe(String name) {
        return subscribers.remove(name) != null;
    }

    /** Returns an unmodifiable view of current subscriber names. */
    public List<String> subscriberNames() {
        return List.copyOf(subscribers.keySet());
    }

    /** Add a listener that is notified when a subscriber throws an exception. */
    public void addErrorListener(FanoutErrorListener listener) {
        if (listener != null) {
            errorListeners.add(listener);
        }
    }

    /**
     * Dispatch a LogEntry to all registered subscribers.
     * If {@link FanoutConfig#isFailFast()} is true the first subscriber exception
     * is rethrown; otherwise errors are forwarded to error listeners and fanout continues.
     */
    public void dispatch(LogEntry entry) {
        if (entry == null) {
            return;
        }
        for (Map.Entry<String, Consumer<LogEntry>> e : subscribers.entrySet()) {
            try {
                e.getValue().accept(entry);
            } catch (Exception ex) {
                if (config.isFailFast()) {
                    throw new FanoutException("Subscriber '" + e.getKey() + "' failed", ex);
                }
                notifyError(e.getKey(), entry, ex);
            }
        }
    }

    public int subscriberCount() {
        return subscribers.size();
    }

    private void notifyError(String subscriberName, LogEntry entry, Exception ex) {
        FanoutException fe = new FanoutException(
                "Subscriber '" + subscriberName + "' threw an exception", ex);
        for (FanoutErrorListener listener : errorListeners) {
            try {
                listener.onError(subscriberName, entry, fe);
            } catch (Exception ignored) {
                // error listeners must not propagate
            }
        }
    }
}
