package com.blog.platform.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * JVM优化配置
 * 提供JVM监控和自动调优功能
 */
@Configuration
@EnableScheduling
public class JvmOptimizationConfig {
    
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JvmOptimizationConfig.class);
    
    /**
     * 注册JVM指标监控
     */
    @Autowired
    public void configure(MeterRegistry registry) {

        // JVM内存指标
        new JvmMemoryMetrics().bindTo(registry);

        // JVM GC指标
        new JvmGcMetrics().bindTo(registry);

        // JVM线程指标
        new JvmThreadMetrics().bindTo(registry);

        // JVM类加载指标
        new ClassLoaderMetrics().bindTo(registry);

        // 系统CPU指标
        new ProcessorMetrics().bindTo(registry);

        // 系统运行时间指标
        new UptimeMetrics().bindTo(registry);

        // 添加通用标签
        registry.config().commonTags(
                "application", "blog-platform",
                "environment", System.getProperty("spring.profiles.active", "default")
        );
    }

/*
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // JVM内存指标
            new JvmMemoryMetrics().bindTo(registry);
            
            // JVM GC指标
            new JvmGcMetrics().bindTo(registry);
            
            // JVM线程指标
            new JvmThreadMetrics().bindTo(registry);
            
            // JVM类加载指标
            new ClassLoaderMetrics().bindTo(registry);
            
            // 系统CPU指标
            new ProcessorMetrics().bindTo(registry);
            
            // 系统运行时间指标
            new UptimeMetrics().bindTo(registry);
            
            // 添加通用标签
            registry.config().commonTags(
                "application", "blog-platform",
                "environment", System.getProperty("spring.profiles.active", "default")
            );
        };
    }*/
    
    /**
     * JVM健康检查和自动调优
     * 每分钟检查一次内存使用情况
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void monitorJvmHealth() {
        try {
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            
            long heapUsed = heapMemoryUsage.getUsed();
            long heapMax = heapMemoryUsage.getMax();
            long nonHeapUsed = nonHeapMemoryUsage.getUsed();
            long nonHeapMax = nonHeapMemoryUsage.getMax();
            
            double heapUsagePercent = (double) heapUsed / heapMax * 100;
            double nonHeapUsagePercent = nonHeapMax > 0 ? (double) nonHeapUsed / nonHeapMax * 100 : 0;
            
            // 记录内存使用情况
            long heapUsedMB = heapUsed / 1024 / 1024;
            long heapMaxMB = heapMax / 1024 / 1024;
            long nonHeapUsedMB = nonHeapUsed / 1024 / 1024;
            long nonHeapMaxMB = nonHeapMax > 0 ? nonHeapMax / 1024 / 1024 : 0;
            
            logger.debug("JVM Memory Usage - Heap: {}% ({}MB/{}MB), Non-Heap: {}% ({}MB/{}MB)",
                String.format("%.2f", heapUsagePercent), heapUsedMB, heapMaxMB,
                String.format("%.2f", nonHeapUsagePercent), nonHeapUsedMB, nonHeapMaxMB);
            
            // 内存使用率告警
            if (heapUsagePercent > 85) {
                logger.warn("Heap memory usage too high: {}%, check for memory leaks or increase heap size", 
                    String.format("%.2f", heapUsagePercent));
                
                // 触发GC建议
                if (heapUsagePercent > 90) {
                    logger.warn("Heap memory usage exceeds 90%, suggesting garbage collection");
                    System.gc();
                }
            }
            
            if (nonHeapUsagePercent > 80 && nonHeapMax > 0) {
                logger.warn("Non-heap memory usage too high: {}%, possible class loading issue", 
                    String.format("%.2f", nonHeapUsagePercent));
            }
            
        } catch (Exception e) {
            logger.error("JVM健康检查失败", e);
        }
    }
    
    /**
     * 获取JVM运行时信息
     */
    public JvmInfo getJvmInfo() {
        Runtime runtime = Runtime.getRuntime();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        
        return JvmInfo.builder()
            .totalMemory(runtime.totalMemory())
            .freeMemory(runtime.freeMemory())
            .maxMemory(runtime.maxMemory())
            .usedMemory(runtime.totalMemory() - runtime.freeMemory())
            .heapUsed(heapMemoryUsage.getUsed())
            .heapMax(heapMemoryUsage.getMax())
            .heapCommitted(heapMemoryUsage.getCommitted())
            .availableProcessors(runtime.availableProcessors())
            .jvmVersion(System.getProperty("java.version"))
            .jvmVendor(System.getProperty("java.vendor"))
            .build();
    }
    
    /**
     * JVM信息数据类
     */
    public static class JvmInfo {
        private long totalMemory;
        private long freeMemory;
        private long maxMemory;
        private long usedMemory;
        private long heapUsed;
        private long heapMax;
        private long heapCommitted;
        private int availableProcessors;
        private String jvmVersion;
        private String jvmVendor;
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private JvmInfo jvmInfo = new JvmInfo();
            
            public Builder totalMemory(long totalMemory) {
                jvmInfo.totalMemory = totalMemory;
                return this;
            }
            
            public Builder freeMemory(long freeMemory) {
                jvmInfo.freeMemory = freeMemory;
                return this;
            }
            
            public Builder maxMemory(long maxMemory) {
                jvmInfo.maxMemory = maxMemory;
                return this;
            }
            
            public Builder usedMemory(long usedMemory) {
                jvmInfo.usedMemory = usedMemory;
                return this;
            }
            
            public Builder heapUsed(long heapUsed) {
                jvmInfo.heapUsed = heapUsed;
                return this;
            }
            
            public Builder heapMax(long heapMax) {
                jvmInfo.heapMax = heapMax;
                return this;
            }
            
            public Builder heapCommitted(long heapCommitted) {
                jvmInfo.heapCommitted = heapCommitted;
                return this;
            }
            
            public Builder availableProcessors(int availableProcessors) {
                jvmInfo.availableProcessors = availableProcessors;
                return this;
            }
            
            public Builder jvmVersion(String jvmVersion) {
                jvmInfo.jvmVersion = jvmVersion;
                return this;
            }
            
            public Builder jvmVendor(String jvmVendor) {
                jvmInfo.jvmVendor = jvmVendor;
                return this;
            }
            
            public JvmInfo build() {
                return jvmInfo;
            }
        }
        
        // Getters
        public long getTotalMemory() { return totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public long getMaxMemory() { return maxMemory; }
        public long getUsedMemory() { return usedMemory; }
        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getHeapCommitted() { return heapCommitted; }
        public int getAvailableProcessors() { return availableProcessors; }
        public String getJvmVersion() { return jvmVersion; }
        public String getJvmVendor() { return jvmVendor; }
        
        public double getMemoryUsagePercent() {
            return maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
        }
        
        public double getHeapUsagePercent() {
            return heapMax > 0 ? (double) heapUsed / heapMax * 100 : 0;
        }
    }
}