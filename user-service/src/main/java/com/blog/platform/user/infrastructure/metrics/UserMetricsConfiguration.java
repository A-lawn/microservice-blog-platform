package com.blog.platform.user.infrastructure.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class UserMetricsConfiguration {

    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong totalRegistrations = new AtomicLong(0);
    private final AtomicLong failedLogins = new AtomicLong(0);

    @Bean
    public Gauge activeUsersGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("user_active_sessions", activeUsers, AtomicLong::get)
                .description("Number of active user sessions")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    public AtomicLong getActiveUsers() {
        return activeUsers;
    }

    public AtomicLong getTotalRegistrations() {
        return totalRegistrations;
    }

    public AtomicLong getFailedLogins() {
        return failedLogins;
    }
}
