package com.blog.platform.common.tracing;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class TracingInterceptor implements HandlerInterceptor {
    
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String SPAN_ID_MDC_KEY = "spanId";
    
    @Autowired(required = false)
    private Tracer tracer;
    
    @Override
    public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
        String traceId = extractTraceId(request);
        String spanId = generateSpanId();
        
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        MDC.put(SPAN_ID_MDC_KEY, spanId);
        
        response.setHeader(TRACE_ID_HEADER, traceId);
        response.setHeader(SPAN_ID_HEADER, spanId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, 
                                 Object handler, Exception ex) {
        MDC.remove(TRACE_ID_MDC_KEY);
        MDC.remove(SPAN_ID_MDC_KEY);
    }
    
    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("traceparent");
            if (traceId != null && traceId.contains("-")) {
                String[] parts = traceId.split("-");
                if (parts.length >= 2) {
                    traceId = parts[1];
                }
            }
        }
        
        if (traceId == null || traceId.isEmpty()) {
            if (tracer != null) {
                var span = tracer.currentSpan();
                if (span != null) {
                    traceId = span.context().traceId();
                }
            }
        }
        
        if (traceId == null || traceId.isEmpty()) {
            traceId = java.util.UUID.randomUUID().toString().replace("-", "");
        }
        
        return traceId;
    }
    
    private String generateSpanId() {
        if (tracer != null) {
            var span = tracer.currentSpan();
            if (span != null) {
                return span.context().spanId();
            }
        }
        return java.util.UUID.randomUUID().toString().substring(0, 16);
    }
}
