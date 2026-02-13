package com.blog.platform.common.tracing;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

public class TraceIdConverter extends ClassicConverter {
    
    private static final String TRACE_ID_KEY = "traceId";
    private static final String OTLP_TRACE_ID_KEY = "trace_id";
    
    @Override
    public String convert(ILoggingEvent event) {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.isEmpty()) {
            traceId = MDC.get(OTLP_TRACE_ID_KEY);
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = event.getMDCPropertyMap().get(TRACE_ID_KEY);
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = event.getMDCPropertyMap().get(OTLP_TRACE_ID_KEY);
        }
        return traceId != null ? traceId : "-";
    }
}
