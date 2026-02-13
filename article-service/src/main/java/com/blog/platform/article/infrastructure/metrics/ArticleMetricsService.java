package com.blog.platform.article.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleMetricsService {

    private final Counter articleCreationCounter;
    private final Counter articlePublishCounter;
    private final Counter articleViewCounter;
    private final Counter articleUpdateCounter;
    private final Counter articleSearchCounter;
    private final Timer articleCreationTimer;
    private final Timer articlePublishTimer;
    private final Timer articleSearchTimer;
    private final ArticleMetricsConfiguration metricsConfiguration;
    private final MeterRegistry meterRegistry;

    @Autowired
    public ArticleMetricsService(MeterRegistry meterRegistry,
                                ArticleMetricsConfiguration metricsConfiguration) {
        this.meterRegistry = meterRegistry;
        this.metricsConfiguration = metricsConfiguration;
        
        this.articleCreationCounter = Counter.builder("article.creations")
            .description("Number of article creations")
            .register(meterRegistry);
        this.articlePublishCounter = Counter.builder("article.publications")
            .description("Number of article publications")
            .register(meterRegistry);
        this.articleViewCounter = Counter.builder("article.views")
            .description("Number of article views")
            .register(meterRegistry);
        this.articleUpdateCounter = Counter.builder("article.updates")
            .description("Number of article updates")
            .register(meterRegistry);
        this.articleSearchCounter = Counter.builder("article.searches")
            .description("Number of article searches")
            .register(meterRegistry);
        this.articleCreationTimer = Timer.builder("article.creation.time")
            .description("Time taken for article creation")
            .register(meterRegistry);
        this.articlePublishTimer = Timer.builder("article.publication.time")
            .description("Time taken for article publication")
            .register(meterRegistry);
        this.articleSearchTimer = Timer.builder("article.search.time")
            .description("Time taken for article search")
            .register(meterRegistry);
    }

    public void recordArticleCreation() {
        articleCreationCounter.increment();
        metricsConfiguration.getTotalArticles().incrementAndGet();
        metricsConfiguration.getDraftArticles().incrementAndGet();
    }

    public void recordArticlePublication() {
        articlePublishCounter.increment();
        metricsConfiguration.getPublishedArticles().incrementAndGet();
        metricsConfiguration.getDraftArticles().decrementAndGet();
    }

    public void recordArticleView() {
        articleViewCounter.increment();
        metricsConfiguration.getTotalViews().incrementAndGet();
    }

    public void recordArticleUpdate() {
        articleUpdateCounter.increment();
    }

    public void recordArticleSearch() {
        articleSearchCounter.increment();
    }

    public Timer.Sample startCreationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopCreationTimer(Timer.Sample sample) {
        sample.stop(articleCreationTimer);
    }

    public Timer.Sample startPublishTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopPublishTimer(Timer.Sample sample) {
        sample.stop(articlePublishTimer);
    }

    public Timer.Sample startSearchTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopSearchTimer(Timer.Sample sample) {
        sample.stop(articleSearchTimer);
    }
}
