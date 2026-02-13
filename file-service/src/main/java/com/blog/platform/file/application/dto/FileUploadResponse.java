package com.blog.platform.file.application.dto;

import com.blog.platform.file.domain.model.FileInfo;

public class FileUploadResponse {
    
    private String id;
    private String originalName;
    private String contentType;
    private long size;
    private String url;
    private String thumbnailUrl;
    
    public static FileUploadResponse from(FileInfo fileInfo) {
        FileUploadResponse response = new FileUploadResponse();
        response.setId(fileInfo.getId());
        response.setOriginalName(fileInfo.getOriginalName());
        response.setContentType(fileInfo.getContentType());
        response.setSize(fileInfo.getSize());
        response.setUrl(fileInfo.getUrl());
        response.setThumbnailUrl(fileInfo.getThumbnailUrl());
        return response;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
