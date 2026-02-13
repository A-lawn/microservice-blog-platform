package com.blog.platform.article.infrastructure.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class ArticleMetricsConfiguration {

    private final AtomicLong totalArticles = new AtomicLong(0);
    private final AtomicLong publishedArticles = new AtomicLong(0);
    private final AtomicLong draftArticles = new AtomicLong(0);
    private final AtomicLong totalViews = new AtomicLong(0);

    @Bean
    public Gauge totalArticlesGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("article_total_count", totalArticles, AtomicLong::get)
                .description("Total number of articles in the system")
                .tag("service", "article-service")
                .register(meterRegistry);
    }

    @Bean
    public Gauge publishedArticlesGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("article_published_count", publishedArticles, AtomicLong::get)
                .description("Number of published articles")
                .tag("service", "article-service")
                .register(meterRegistry);
    }

    @Bean
    public Gauge draftArticlesGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("article_draft_count", draftArticles, AtomicLong::get)
                .description("Number of draft articles")
                .tag("service", "article-service")
                .register(meterRegistry);
    }

    public AtomicLong getTotalArticles() {
        return totalArticles;
    }

    public AtomicLong getPublishedArticles() {
        return publishedArticles;
    }

    public AtomicLong getDraftArticles() {
        return draftArticles;
    }

    public AtomicLong getTotalViews() {
        return totalViews;
    }
}
