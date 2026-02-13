package com.blog.platform.common.distributedtx;

import java.util.Map;

public interface SagaStep {
    
    String getName();
    
    void execute(Map<String, Object> context);
    
    void compensate(Map<String, Object> context);
    
    default int getOrder() {
        return 0;
    }
}
