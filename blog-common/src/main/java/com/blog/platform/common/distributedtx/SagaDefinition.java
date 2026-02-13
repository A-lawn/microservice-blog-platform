package com.blog.platform.common.distributedtx;

import java.util.List;
import java.util.Map;

public interface SagaDefinition {
    
    String getName();
    
    List<SagaStep> getSteps();
    
    default int getTimeout() {
        return 60000;
    }
    
    default int getRetryCount() {
        return 3;
    }
}
