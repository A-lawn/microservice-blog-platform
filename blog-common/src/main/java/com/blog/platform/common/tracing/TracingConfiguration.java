package com.blog.platform.common.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 分布式链路追踪配置
 * 提供统一的链路追踪功能
 */
@Configuration
public class TracingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TracingConfiguration.class);

    @Component
    public static class TracingService {

        private final Tracer tracer;

        @Autowired
        public TracingService(Tracer tracer) {
            this.tracer = tracer;
        }

        /**
         * 创建自定义Span
         */
        @NewSpan("business-operation")
        public <T> T traceBusinessOperation(@SpanTag("operation") String operation,
                                          @SpanTag("userId") String userId,
                                          @SpanTag("resourceId") String resourceId,
                                          TracedOperation<T> tracedOperation) {
            Span span = tracer.nextSpan()
                    .name("business-operation")
                    .tag("operation", operation)
                    .tag("userId", userId)
                    .tag("resourceId", resourceId)
                    .start();
            try {
                return tracedOperation.execute();
            } catch (Exception ex) {
                log.info(ex.getMessage());
            } finally {
                span.end();
            }
            return null;
        }

        /**
         * 创建数据库操作Span
         */
        @NewSpan("database-operation")
        public <T> T traceDatabaseOperation(@SpanTag("table") String table,
                                          @SpanTag("operation") String operation,
                                          TracedOperation<T> tracedOperation) {

            Span span = tracer.nextSpan()
                    .name("database-operation")
                    .tag("db.table", table)
                    .tag("db.operation", operation)
                    .start();
            try {
                return tracedOperation.execute();
            } catch (Exception ex) {
                log.info(ex.getMessage());
            } finally {
                span.end();
            }
            return null;
        }

        /**
         * 创建外部服务调用Span
         */
        @NewSpan("external-service-call")
        public <T> T traceExternalServiceCall(@SpanTag("service") String service,
                                            @SpanTag("method") String method,
                                            @SpanTag("endpoint") String endpoint,
                                            TracedOperation<T> tracedOperation) {

            Span span = tracer.nextSpan()
                    .name("external-service-call")
                    .tag("service.name", service)
                    .tag("http.method", method)
                    .tag("http.url", endpoint)
                    .start();
            try {
                return tracedOperation.execute();
            } catch (Exception ex) {
                log.info(ex.getMessage());
            } finally {
                span.end();
            }
            return null;
        }

        /**
         * 创建消息处理Span
         */
        @NewSpan("message-processing")
        public <T> T traceMessageProcessing(@SpanTag("topic") String topic,
                                          @SpanTag("messageType") String messageType,
                                          TracedOperation<T> tracedOperation) {

            Span span = tracer.nextSpan()
                    .name("message-processing")
                    .tag("messaging.topic", topic)
                    .tag("messaging.message_type", messageType)
                    .start();
            try {
                return tracedOperation.execute();
            } catch (Exception ex) {
                log.info(ex.getMessage());
            } finally {
                span.end();
            }
            return null;
        }

        /**
         * 添加自定义标签到当前Span
         */
        public void addSpanTag(String key, String value) {
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().tag(key, value);
            }
        }

        /**
         * 添加事件到当前Span
         */
        public void addSpanEvent(String event) {
            if (tracer.currentSpan() != null) {
                tracer.currentSpan().event(event);
            }
        }

        /**
         * 获取当前TraceId
         */
        public String getCurrentTraceId() {
            if (tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
                return tracer.currentSpan().context().traceId();
            }
            return null;
        }

        /**
         * 获取当前SpanId
         */
        public String getCurrentSpanId() {
            if (tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
                return tracer.currentSpan().context().spanId();
            }
            return null;
        }
    }

    @FunctionalInterface
    public interface TracedOperation<T> {
        T execute() throws Exception;
    }
}