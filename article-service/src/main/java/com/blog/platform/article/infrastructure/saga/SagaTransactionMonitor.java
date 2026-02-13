package com.blog.platform.article.infrastructure.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga事务监控器
 * 监控和记录Saga事务的执行状态和告警
 */
@Component
public class SagaTransactionMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaTransactionMonitor.class);
    
    // 存储正在执行的事务状态
    private final Map<String, TransactionStatus> activeTransactions = new ConcurrentHashMap<>();
    
    /**
     * 记录事务开始
     */
    public void recordTransactionStart(String transactionId, String type, Map<String, Object> context) {
        TransactionStatus status = new TransactionStatus(
                transactionId, 
                type, 
                LocalDateTime.now(), 
                "STARTED",
                context
        );
        
        activeTransactions.put(transactionId, status);
        logger.info("Saga transaction started: {} of type: {}", transactionId, type);
    }
    
    /**
     * 记录事务步骤完成
     */
    public void recordStepCompleted(String transactionId, String stepName, boolean success, String error) {
        TransactionStatus status = activeTransactions.get(transactionId);
        if (status != null) {
            status.addStep(stepName, success, error);
            
            if (success) {
                logger.info("Saga step completed successfully: {} - {}", transactionId, stepName);
            } else {
                logger.error("Saga step failed: {} - {} - Error: {}", transactionId, stepName, error);
                // 触发告警
                triggerAlert(transactionId, stepName, error);
            }
        }
    }
    
    /**
     * 记录事务完成
     */
    public void recordTransactionCompleted(String transactionId, boolean success, String error) {
        TransactionStatus status = activeTransactions.get(transactionId);
        if (status != null) {
            status.setEndTime(LocalDateTime.now());
            status.setStatus(success ? "COMPLETED" : "FAILED");
            status.setError(error);
            
            if (success) {
                logger.info("Saga transaction completed successfully: {}", transactionId);
            } else {
                logger.error("Saga transaction failed: {} - Error: {}", transactionId, error);
                triggerAlert(transactionId, "TRANSACTION_FAILED", error);
            }
            
            // 移除已完成的事务（可选，也可以保留用于审计）
            activeTransactions.remove(transactionId);
        }
    }
    
    /**
     * 记录补偿操作
     */
    public void recordCompensation(String transactionId, String stepName, boolean success, String error) {
        logger.warn("Saga compensation executed: {} - {} - Success: {} - Error: {}", 
                   transactionId, stepName, success, error);
        
        if (!success) {
            triggerAlert(transactionId, "COMPENSATION_FAILED", error);
        }
    }
    
    /**
     * 获取活跃事务状态
     */
    public Map<String, TransactionStatus> getActiveTransactions() {
        return new ConcurrentHashMap<>(activeTransactions);
    }
    
    /**
     * 检查超时事务
     */
    public void checkTimeoutTransactions(int timeoutMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        activeTransactions.entrySet().removeIf(entry -> {
            TransactionStatus status = entry.getValue();
            if (status.getStartTime().isBefore(cutoff)) {
                logger.warn("Saga transaction timeout detected: {} - Started: {}", 
                           entry.getKey(), status.getStartTime());
                triggerAlert(entry.getKey(), "TRANSACTION_TIMEOUT", 
                           "Transaction exceeded timeout of " + timeoutMinutes + " minutes");
                return true;
            }
            return false;
        });
    }
    
    /**
     * 触发告警
     */
    private void triggerAlert(String transactionId, String alertType, String message) {
        // 这里可以集成告警系统，如发送邮件、钉钉通知等
        logger.error("SAGA ALERT - Transaction: {} - Type: {} - Message: {}", 
                    transactionId, alertType, message);
        
        // 可以发送到监控系统
        // alertService.sendAlert(transactionId, alertType, message);
    }
    
    /**
     * 事务状态内部类
     */
    public static class TransactionStatus {
        private final String transactionId;
        private final String type;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private String error;
        private final Map<String, Object> context;
        private final Map<String, StepStatus> steps = new ConcurrentHashMap<>();
        
        public TransactionStatus(String transactionId, String type, LocalDateTime startTime, 
                               String status, Map<String, Object> context) {
            this.transactionId = transactionId;
            this.type = type;
            this.startTime = startTime;
            this.status = status;
            this.context = context;
        }
        
        public void addStep(String stepName, boolean success, String error) {
            steps.put(stepName, new StepStatus(stepName, success, error, LocalDateTime.now()));
        }
        
        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public Map<String, Object> getContext() { return context; }
        public Map<String, StepStatus> getSteps() { return steps; }
    }
    
    /**
     * 步骤状态内部类
     */
    public static class StepStatus {
        private final String stepName;
        private final boolean success;
        private final String error;
        private final LocalDateTime timestamp;
        
        public StepStatus(String stepName, boolean success, String error, LocalDateTime timestamp) {
            this.stepName = stepName;
            this.success = success;
            this.error = error;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getStepName() { return stepName; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}