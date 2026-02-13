package com.blog.platform.file.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {
    
    private String type = "minio";
    private MinioConfig minio = new MinioConfig();
    private LocalConfig local = new LocalConfig();
    private List<String> allowedTypes = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "text/plain", "text/markdown"
    );
    private long maxSize = 52428800;
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public MinioConfig getMinio() { return minio; }
    public void setMinio(MinioConfig minio) { this.minio = minio; }
    public LocalConfig getLocal() { return local; }
    public void setLocal(LocalConfig local) { this.local = local; }
    public List<String> getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(List<String> allowedTypes) { this.allowedTypes = allowedTypes; }
    public long getMaxSize() { return maxSize; }
    public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
    
    public static class MinioConfig {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "blog-files";
        private boolean secure = false;
        
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
    }
    
    public static class LocalConfig {
        private String basePath = "./uploads";
        private String tempPath = "./temp";
        
        public String getBasePath() { return basePath; }
        public void setBasePath(String basePath) { this.basePath = basePath; }
        public String getTempPath() { return tempPath; }
        public void setTempPath(String tempPath) { this.tempPath = tempPath; }
    }
}
