package com.blog.platform.comment.infrastructure.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga事务审计日志记录器
 * 记录Saga事务的详细执行日志用于审计和故障排查
 */
@Component
public class SagaAuditLogger {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("SAGA_AUDIT");
    private static final Logger logger = LoggerFactory.getLogger(SagaAuditLogger.class);
    
    // 存储事务执行日志
    private final Map<String, TransactionAuditLog> auditLogs = new ConcurrentHashMap<>();
    
    /**
     * 记录事务开始
     */
    public void logTransactionStart(String transactionId, String sagaType, Map<String, Object> context) {
        TransactionAuditLog auditLog = new TransactionAuditLog(transactionId, sagaType);
        auditLog.addEntry("TRANSACTION_START", "SUCCESS", "Saga transaction started", context);
        
        auditLogs.put(transactionId, auditLog);
        
        auditLogger.info("SAGA_START|{}|{}|{}", transactionId, sagaType, context);
    }
    
    /**
     * 记录步骤执行
     */
    public void logStepExecution(String transactionId, String stepName, String status, 
                                String message, Map<String, Object> stepContext) {
        TransactionAuditLog auditLog = auditLogs.get(transactionId);
        if (auditLog != null) {
            auditLog.addEntry(stepName, status, message, stepContext);
        }
        
        auditLogger.info("SAGA_STEP|{}|{}|{}|{}|{}", transactionId, stepName, status, message, stepContext);
    }
    
    /**
     * 记录补偿操作
     */
    public void logCompensation(String transactionId, String stepName, String status, 
                               String message, Map<String, Object> compensationContext) {
        TransactionAuditLog auditLog = auditLogs.get(transactionId);
        if (auditLog != null) {
            auditLog.addEntry("COMPENSATION_" + stepName, status, message, compensationContext);
        }
        
        auditLogger.warn("SAGA_COMPENSATION|{}|{}|{}|{}|{}", transactionId, stepName, status, message, compensationContext);
    }
    
    /**
     * 记录事务完成
     */
    public void logTransactionEnd(String transactionId, String finalStatus, String message, 
                                 Map<String, Object> finalContext) {
        TransactionAuditLog auditLog = auditLogs.get(transactionId);
        if (auditLog != null) {
            auditLog.addEntry("TRANSACTION_END", finalStatus, message, finalContext);
            auditLog.setEndTime(LocalDateTime.now());
            auditLog.setFinalStatus(finalStatus);
        }
        
        auditLogger.info("SAGA_END|{}|{}|{}|{}", transactionId, finalStatus, message, finalContext);
        
        // 可选：移除已完成的审计日志以节省内存（或者持久化到数据库）
        // auditLogs.remove(transactionId);
    }
    
    /**
     * 记录错误信息
     */
    public void logError(String transactionId, String stepName, String errorMessage, Exception exception) {
        Map<String, Object> errorContext = new ConcurrentHashMap<>();
        errorContext.put("errorMessage", errorMessage);
        errorContext.put("exceptionType", exception != null ? exception.getClass().getSimpleName() : "Unknown");
        errorContext.put("stackTrace", exception != null ? getStackTraceString(exception) : "N/A");
        
        TransactionAuditLog auditLog = auditLogs.get(transactionId);
        if (auditLog != null) {
            auditLog.addEntry("ERROR_" + stepName, "FAILED", errorMessage, errorContext);
        }
        
        auditLogger.error("SAGA_ERROR|{}|{}|{}|{}", transactionId, stepName, errorMessage, errorContext);
    }
    
    /**
     * 获取事务审计日志
     */
    public TransactionAuditLog getAuditLog(String transactionId) {
        return auditLogs.get(transactionId);
    }
    
    /**
     * 获取所有审计日志
     */
    public Map<String, TransactionAuditLog> getAllAuditLogs() {
        return new ConcurrentHashMap<>(auditLogs);
    }
    
    /**
     * 清理过期的审计日志
     */
    public void cleanupExpiredLogs(int retentionHours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(retentionHours);
        
        auditLogs.entrySet().removeIf(entry -> {
            TransactionAuditLog log = entry.getValue();
            return log.getStartTime().isBefore(cutoff);
        });
        
        logger.info("Cleaned up expired audit logs older than {} hours", retentionHours);
    }
    
    /**
     * 获取异常堆栈跟踪字符串
     */
    private String getStackTraceString(Exception exception) {
        if (exception == null) return "N/A";
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * 事务审计日志内部类
     */
    public static class TransactionAuditLog {
        private final String transactionId;
        private final String sagaType;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private String finalStatus;
        private final java.util.List<AuditEntry> entries = new java.util.ArrayList<>();
        
        public TransactionAuditLog(String transactionId, String sagaType) {
            this.transactionId = transactionId;
            this.sagaType = sagaType;
            this.startTime = LocalDateTime.now();
        }
        
        public void addEntry(String operation, String status, String message, Map<String, Object> context) {
            entries.add(new AuditEntry(operation, status, message, context, LocalDateTime.now()));
        }
        
        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public String getSagaType() { return sagaType; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getFinalStatus() { return finalStatus; }
        public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
        public java.util.List<AuditEntry> getEntries() { return entries; }
    }
    
    /**
     * 审计条目内部类
     */
    public static class AuditEntry {
        private final String operation;
        private final String status;
        private final String message;
        private final Map<String, Object> context;
        private final LocalDateTime timestamp;
        
        public AuditEntry(String operation, String status, String message, 
                         Map<String, Object> context, LocalDateTime timestamp) {
            this.operation = operation;
            this.status = status;
            this.message = message;
            this.context = context;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getOperation() { return operation; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Map<String, Object> getContext() { return context; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}