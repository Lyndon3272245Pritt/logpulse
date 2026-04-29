package com.logpulse.routing;

import com.logpulse.filter.LogFilter;
import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogRouterTest {

    private LogRouter router;

    @BeforeEach
    void setUp() {
        router = new LogRouter();
    }

    private LogEntry entry(String service, String level, String message) {
        return new LogEntry(Instant.now(), service, level, message);
    }

    @Test
    void routeDeliversToMatchingService() {
        List<LogEntry> received = new ArrayList<>();
        router.register("auth-service", null, received::add);

        LogEntry e = entry("auth-service", "INFO", "user logged in");
        router.route(e);

        assertEquals(1, received.size());
        assertSame(e, received.get(0));
    }

    @Test
    void routeDoesNotDeliverToOtherService() {
        List<LogEntry> received = new ArrayList<>();
        router.register("auth-service", null, received::add);

        router.route(entry("payment-service", "ERROR", "timeout"));

        assertTrue(received.isEmpty());
    }

    @Test
    void wildcardRouteReceivesAllServices() {
        List<LogEntry> received = new ArrayList<>();
        router.register("*", null, received::add);

        router.route(entry("auth-service", "INFO", "login"));
        router.route(entry("payment-service", "ERROR", "fail"));

        assertEquals(2, received.size());
    }

    @Test
    void filterIsAppliedBeforeRouting() {
        List<LogEntry> received = new ArrayList<>();
        LogFilter errorOnly = e -> "ERROR".equalsIgnoreCase(e.getLevel());
        router.register("api-service", errorOnly, received::add);

        router.route(entry("api-service", "INFO", "request ok"));
        router.route(entry("api-service", "ERROR", "bad gateway"));

        assertEquals(1, received.size());
        assertEquals("ERROR", received.get(0).getLevel());
    }

    @Test
    void routeCountReflectsRegistrations() {
        router.register("svc-a", null, e -> {});
        router.register("svc-a", null, e -> {});
        router.register("svc-b", null, e -> {});

        assertEquals(3, router.routeCount());
    }

    @Test
    void clearRemovesAllRoutes() {
        router.register("svc-a", null, e -> {});
        router.clear();

        assertEquals(0, router.routeCount());
    }

    @Test
    void registerThrowsOnNullConsumer() {
        assertThrows(IllegalArgumentException.class,
                () -> router.register("svc", null, null));
    }

    @Test
    void routeConfigBuilderCreatesCorrectConfig() {
        RouteConfig config = RouteConfig.builder("order-service")
                .withLevel("warn")
                .withLevel("error")
                .withMessageContains("timeout")
                .build();

        assertEquals("order-service", config.getServiceKey());
        assertEquals(List.of("WARN", "ERROR"), config.getLevels());
        assertEquals("timeout", config.getMessageContains());
    }
}
