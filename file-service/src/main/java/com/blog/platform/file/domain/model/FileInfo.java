package com.blog.platform.file.domain.model;

import java.time.LocalDateTime;

public class FileInfo {
    
    private String id;
    private String originalName;
    private String storedName;
    private String contentType;
    private long size;
    private String url;
    private String thumbnailUrl;
    private String bucket;
    private String path;
    private String md5;
    private String uploaderId;
    private LocalDateTime createdAt;
    
    public FileInfo() {}
    
    public FileInfo(String id, String originalName, String storedName, String contentType, 
                   long size, String url, String uploaderId) {
        this.id = id;
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.size = size;
        this.url = url;
        this.uploaderId = uploaderId;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getMd5() { return md5; }
    public void setMd5(String md5) { this.md5 = md5; }
    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
    
    public String getFileExtension() {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
    }
}
