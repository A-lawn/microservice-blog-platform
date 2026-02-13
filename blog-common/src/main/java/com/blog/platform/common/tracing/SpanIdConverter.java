package com.blog.platform.common.tracing;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

public class SpanIdConverter extends ClassicConverter {
    
    private static final String SPAN_ID_KEY = "spanId";
    private static final String OTLP_SPAN_ID_KEY = "span_id";
    
    @Override
    public String convert(ILoggingEvent event) {
        String spanId = MDC.get(SPAN_ID_KEY);
        if (spanId == null || spanId.isEmpty()) {
            spanId = MDC.get(OTLP_SPAN_ID_KEY);
        }
        if (spanId == null || spanId.isEmpty()) {
            spanId = event.getMDCPropertyMap().get(SPAN_ID_KEY);
        }
        if (spanId == null || spanId.isEmpty()) {
            spanId = event.getMDCPropertyMap().get(OTLP_SPAN_ID_KEY);
        }
        return spanId != null ? spanId : "-";
    }
}
