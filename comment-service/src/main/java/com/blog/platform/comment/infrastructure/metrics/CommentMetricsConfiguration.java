package com.blog.platform.comment.infrastructure.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class CommentMetricsConfiguration {

    private final AtomicLong totalComments = new AtomicLong(0);
    private final AtomicLong activeComments = new AtomicLong(0);
    private final AtomicLong moderatedComments = new AtomicLong(0);

    @Bean
    public Gauge totalCommentsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("comment_total_count", totalComments, AtomicLong::get)
                .description("Total number of comments in the system")
                .tag("service", "comment-service")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeCommentsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("comment_active_count", activeComments, AtomicLong::get)
                .description("Number of active comments")
                .tag("service", "comment-service")
                .register(meterRegistry);
    }

    @Bean
    public Gauge moderatedCommentsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("comment_moderated_count", moderatedComments, AtomicLong::get)
                .description("Number of moderated comments")
                .tag("service", "comment-service")
                .register(meterRegistry);
    }

    public AtomicLong getTotalComments() {
        return totalComments;
    }

    public AtomicLong getActiveComments() {
        return activeComments;
    }

    public AtomicLong getModeratedComments() {
        return moderatedComments;
    }
}
