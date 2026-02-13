package com.blog.platform.file.infrastructure.service;

import com.blog.platform.file.domain.model.FileInfo;
import com.blog.platform.file.domain.service.FileStorageService;
import com.blog.platform.file.infrastructure.config.FileStorageProperties;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "minio")
public class MinioFileStorageService implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MinioFileStorageService.class);
    
    private final MinioClient minioClient;
    private final FileStorageProperties properties;
    
    public MinioFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        FileStorageProperties.MinioConfig minioConfig = properties.getMinio();
        
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey());
        
        this.minioClient = builder.build();
        
        ensureBucketExists();
    }
    
    private void ensureBucketExists() {
        try {
            String bucket = properties.getMinio().getBucket();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());
            
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());
                logger.info("Created bucket: {}", bucket);
            }
        } catch (Exception e) {
            logger.error("Failed to ensure bucket exists", e);
        }
    }
    
    @Override
    public FileInfo upload(MultipartFile file, String uploaderId) {
        try {
            return upload(file.getInputStream(), file.getOriginalFilename(), 
                         file.getContentType(), file.getSize(), uploaderId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    
    @Override
    public FileInfo upload(InputStream inputStream, String originalName, String contentType, 
                          long size, String uploaderId) {
        try {
            String fileId = UUID.randomUUID().toString();
            String extension = getFileExtension(originalName);
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String storedName = fileId + (extension.isEmpty() ? "" : "." + extension);
            String objectName = datePath + "/" + storedName;
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            
            String url = buildUrl(objectName);
            
            FileInfo fileInfo = new FileInfo(fileId, originalName, storedName, contentType, 
                                            size, url, uploaderId);
            fileInfo.setBucket(properties.getMinio().getBucket());
            fileInfo.setPath(objectName);
            
            logger.info("File uploaded successfully: {} -> {}", originalName, objectName);
            
            return fileInfo;
            
        } catch (Exception e) {
            logger.error("Failed to upload file: {}", originalName, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream download(String fileId) {
        try {
            FileInfo fileInfo = getFileInfo(fileId);
            if (fileInfo == null) {
                throw new RuntimeException("File not found: " + fileId);
            }
            
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileInfo.getPath())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + fileId, e);
        }
    }
    
    @Override
    public void delete(String fileId) {
        try {
            FileInfo fileInfo = getFileInfo(fileId);
            if (fileInfo != null) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(properties.getMinio().getBucket())
                        .object(fileInfo.getPath())
                        .build());
                logger.info("File deleted: {}", fileId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + fileId, e);
        }
    }
    
    @Override
    public FileInfo getFileInfo(String fileId) {
        return null;
    }
    
    @Override
    public String getPresignedUrl(String fileId, int expiresInSeconds) {
        try {
            FileInfo fileInfo = getFileInfo(fileId);
            if (fileInfo == null) {
                throw new RuntimeException("File not found: " + fileId);
            }
            
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getMinio().getBucket())
                    .object(fileInfo.getPath())
                    .expiry(expiresInSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
    
    @Override
    public List<FileInfo> listFiles(String prefix, int limit) {
        return List.of();
    }
    
    @Override
    public boolean exists(String fileId) {
        try {
            FileInfo fileInfo = getFileInfo(fileId);
            return fileInfo != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String buildUrl(String objectName) {
        return String.format("%s/%s/%s", 
                properties.getMinio().getEndpoint(),
                properties.getMinio().getBucket(),
                objectName);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
