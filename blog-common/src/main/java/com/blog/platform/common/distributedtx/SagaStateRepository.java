package com.blog.platform.common.distributedtx;

import com.blog.platform.common.distributedtx.SagaOrchestrator.SagaState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaStateRepository {
    
    private final Map<String, SagaState> stateStore = new ConcurrentHashMap<>();
    
    public void save(SagaState state) {
        stateStore.put(state.getSagaId(), state);
    }
    
    public SagaState findById(String sagaId) {
        return stateStore.get(sagaId);
    }
    
    public void delete(String sagaId) {
        stateStore.remove(sagaId);
    }
    
    public Map<String, SagaState> findAll() {
        return new ConcurrentHashMap<>(stateStore);
    }
}
