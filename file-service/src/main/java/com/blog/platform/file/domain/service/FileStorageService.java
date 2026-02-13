package com.blog.platform.file.domain.service;

import com.blog.platform.file.domain.model.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileStorageService {
    
    FileInfo upload(MultipartFile file, String uploaderId);
    
    FileInfo upload(InputStream inputStream, String originalName, String contentType, 
                   long size, String uploaderId);
    
    InputStream download(String fileId);
    
    void delete(String fileId);
    
    FileInfo getFileInfo(String fileId);
    
    String getPresignedUrl(String fileId, int expiresInSeconds);
    
    List<FileInfo> listFiles(String prefix, int limit);
    
    boolean exists(String fileId);
}
