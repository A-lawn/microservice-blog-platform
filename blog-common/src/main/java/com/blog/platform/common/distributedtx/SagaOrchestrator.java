package com.blog.platform.common.distributedtx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaOrchestrator.class);
    
    private final Map<String, SagaDefinition> sagaDefinitions = new ConcurrentHashMap<>();
    private final SagaStateRepository stateRepository;
    
    public SagaOrchestrator(SagaStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }
    
    public void registerSaga(String sagaName, SagaDefinition definition) {
        sagaDefinitions.put(sagaName, definition);
        logger.info("Registered saga: {}", sagaName);
    }
    
    public SagaExecutionResult execute(String sagaName, Map<String, Object> initialContext) {
        String sagaId = UUID.randomUUID().toString();
        SagaDefinition definition = sagaDefinitions.get(sagaName);
        
        if (definition == null) {
            throw new IllegalArgumentException("Saga not found: " + sagaName);
        }
        
        SagaState state = new SagaState(sagaId, sagaName, initialContext);
        stateRepository.save(state);
        
        logger.info("Starting saga execution: {} with id: {}", sagaName, sagaId);
        
        try {
            List<SagaStep> steps = definition.getSteps();
            Map<String, Object> context = new HashMap<>(initialContext);
            
            for (int i = 0; i < steps.size(); i++) {
                SagaStep step = steps.get(i);
                state.setCurrentStep(i);
                state.setStatus(SagaStatus.EXECUTING);
                stateRepository.save(state);
                
                logger.info("Executing step {} of saga {}: {}", i + 1, sagaName, step.getName());
                
                try {
                    step.execute(context);
                    state.addCompletedStep(i);
                    stateRepository.save(state);
                    
                } catch (Exception e) {
                    logger.error("Step {} failed in saga {}: {}", i + 1, sagaName, e.getMessage());
                    state.setStatus(SagaStatus.COMPENSATING);
                    state.setErrorMessage(e.getMessage());
                    stateRepository.save(state);
                    
                    compensate(definition, state, context, i - 1);
                    
                    return SagaExecutionResult.failed(sagaId, e.getMessage());
                }
            }
            
            state.setStatus(SagaStatus.COMPLETED);
            stateRepository.save(state);
            logger.info("Saga completed successfully: {}", sagaId);
            
            return SagaExecutionResult.success(sagaId, context);
            
        } catch (Exception e) {
            logger.error("Saga execution failed: {}", sagaId, e);
            state.setStatus(SagaStatus.FAILED);
            state.setErrorMessage(e.getMessage());
            stateRepository.save(state);
            
            return SagaExecutionResult.failed(sagaId, e.getMessage());
        }
    }
    
    private void compensate(SagaDefinition definition, SagaState state, 
                           Map<String, Object> context, int fromStep) {
        logger.info("Starting compensation for saga {} from step {}", state.getSagaId(), fromStep);
        
        List<SagaStep> steps = definition.getSteps();
        
        for (int i = fromStep; i >= 0; i--) {
            if (state.getCompletedSteps().contains(i)) {
                SagaStep step = steps.get(i);
                
                try {
                    logger.info("Compensating step {} of saga {}", i + 1, state.getSagaName());
                    step.compensate(context);
                    
                } catch (Exception e) {
                    logger.error("Compensation failed for step {} in saga {}: {}", 
                            i + 1, state.getSagaId(), e.getMessage());
                }
            }
        }
        
        state.setStatus(SagaStatus.COMPENSATED);
        stateRepository.save(state);
    }
    
    public enum SagaStatus {
        STARTED, EXECUTING, COMPENSATING, COMPLETED, COMPENSATED, FAILED
    }
    
    public static class SagaState {
        private final String sagaId;
        private final String sagaName;
        private final Map<String, Object> initialContext;
        private SagaStatus status = SagaStatus.STARTED;
        private int currentStep = 0;
        private final Set<Integer> completedSteps = new HashSet<>();
        private String errorMessage;
        private final long startTime = System.currentTimeMillis();
        
        public SagaState(String sagaId, String sagaName, Map<String, Object> initialContext) {
            this.sagaId = sagaId;
            this.sagaName = sagaName;
            this.initialContext = initialContext;
        }
        
        public String getSagaId() { return sagaId; }
        public String getSagaName() { return sagaName; }
        public Map<String, Object> getInitialContext() { return initialContext; }
        public SagaStatus getStatus() { return status; }
        public void setStatus(SagaStatus status) { this.status = status; }
        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
        public Set<Integer> getCompletedSteps() { return completedSteps; }
        public void addCompletedStep(int step) { completedSteps.add(step); }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getStartTime() { return startTime; }
    }
    
    public static class SagaExecutionResult {
        private final String sagaId;
        private final boolean success;
        private final String errorMessage;
        private final Map<String, Object> context;
        
        private SagaExecutionResult(String sagaId, boolean success, String errorMessage, Map<String, Object> context) {
            this.sagaId = sagaId;
            this.success = success;
            this.errorMessage = errorMessage;
            this.context = context;
        }
        
        public static SagaExecutionResult success(String sagaId, Map<String, Object> context) {
            return new SagaExecutionResult(sagaId, true, null, context);
        }
        
        public static SagaExecutionResult failed(String sagaId, String errorMessage) {
            return new SagaExecutionResult(sagaId, false, errorMessage, null);
        }
        
        public String getSagaId() { return sagaId; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getContext() { return context; }
    }
}
