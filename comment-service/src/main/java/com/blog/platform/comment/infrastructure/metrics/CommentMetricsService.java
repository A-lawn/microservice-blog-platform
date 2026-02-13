package com.blog.platform.comment.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentMetricsService {

    private final Counter commentCreationCounter;
    private final Counter commentReplyCounter;
    private final Counter commentModerationCounter;
    private final Counter commentDeleteCounter;
    private final Timer commentCreationTimer;
    private final Timer commentReplyTimer;
    private final Timer commentQueryTimer;
    private final CommentMetricsConfiguration metricsConfiguration;
    private final MeterRegistry meterRegistry;

    @Autowired
    public CommentMetricsService(MeterRegistry meterRegistry,
                                CommentMetricsConfiguration metricsConfiguration) {
        this.meterRegistry = meterRegistry;
        this.metricsConfiguration = metricsConfiguration;
        
        this.commentCreationCounter = Counter.builder("comment.creations")
            .description("Number of comment creations")
            .register(meterRegistry);
        this.commentReplyCounter = Counter.builder("comment.replies")
            .description("Number of comment replies")
            .register(meterRegistry);
        this.commentModerationCounter = Counter.builder("comment.moderations")
            .description("Number of comment moderations")
            .register(meterRegistry);
        this.commentDeleteCounter = Counter.builder("comment.deletions")
            .description("Number of comment deletions")
            .register(meterRegistry);
        this.commentCreationTimer = Timer.builder("comment.creation.time")
            .description("Time taken for comment creation")
            .register(meterRegistry);
        this.commentReplyTimer = Timer.builder("comment.reply.time")
            .description("Time taken for comment reply")
            .register(meterRegistry);
        this.commentQueryTimer = Timer.builder("comment.query.time")
            .description("Time taken for comment query")
            .register(meterRegistry);
    }

    public void recordCommentCreation() {
        commentCreationCounter.increment();
        metricsConfiguration.getTotalComments().incrementAndGet();
        metricsConfiguration.getActiveComments().incrementAndGet();
    }

    public void recordCommentReply() {
        commentReplyCounter.increment();
        metricsConfiguration.getTotalComments().incrementAndGet();
        metricsConfiguration.getActiveComments().incrementAndGet();
    }

    public void recordCommentModeration() {
        commentModerationCounter.increment();
        metricsConfiguration.getModeratedComments().incrementAndGet();
        metricsConfiguration.getActiveComments().decrementAndGet();
    }

    public void recordCommentDeletion() {
        commentDeleteCounter.increment();
        metricsConfiguration.getActiveComments().decrementAndGet();
    }

    public Timer.Sample startCreationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopCreationTimer(Timer.Sample sample) {
        sample.stop(commentCreationTimer);
    }

    public Timer.Sample startReplyTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopReplyTimer(Timer.Sample sample) {
        sample.stop(commentReplyTimer);
    }

    public Timer.Sample startQueryTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopQueryTimer(Timer.Sample sample) {
        sample.stop(commentQueryTimer);
    }
}
