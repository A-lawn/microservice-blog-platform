package com.blog.platform.file.application.service;

import com.blog.platform.file.domain.model.FileInfo;
import com.blog.platform.file.domain.service.FileStorageService;
import com.blog.platform.file.infrastructure.config.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileApplicationService.class);
    
    private final FileStorageService fileStorageService;
    private final FileStorageProperties properties;
    
    public FileApplicationService(FileStorageService fileStorageService, 
                                 FileStorageProperties properties) {
        this.fileStorageService = fileStorageService;
        this.properties = properties;
    }
    
    public FileInfo uploadFile(MultipartFile file, String uploaderId) {
        validateFile(file);
        return fileStorageService.upload(file, uploaderId);
    }
    
    public List<FileInfo> uploadFiles(List<MultipartFile> files, String uploaderId) {
        List<FileInfo> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                validateFile(file);
                FileInfo fileInfo = fileStorageService.upload(file, uploaderId);
                results.add(fileInfo);
            } catch (Exception e) {
                logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            }
        }
        return results;
    }
    
    public InputStream downloadFile(String fileId) {
        return fileStorageService.download(fileId);
    }
    
    public void deleteFile(String fileId) {
        fileStorageService.delete(fileId);
    }
    
    public FileInfo getFileInfo(String fileId) {
        return fileStorageService.getFileInfo(fileId);
    }
    
    public String getPresignedUrl(String fileId, int expiresInSeconds) {
        return fileStorageService.getPresignedUrl(fileId, expiresInSeconds);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > properties.getMaxSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        String contentType = file.getContentType();
        if (contentType != null && !properties.getAllowedTypes().contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }
}
