package com.blog.platform.file.interfaces.controller;

import com.blog.platform.file.application.dto.FileUploadResponse;
import com.blog.platform.file.application.service.FileApplicationService;
import com.blog.platform.file.domain.model.FileInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、删除接口")
public class FileController {
    
    private final FileApplicationService fileApplicationService;
    
    public FileController(FileApplicationService fileApplicationService) {
        this.fileApplicationService = fileApplicationService;
    }
    
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件，支持图片、PDF等格式")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "上传者ID") @RequestHeader(value = "X-User-Id", required = false) String uploaderId) {
        
        FileInfo fileInfo = fileApplicationService.uploadFile(file, uploaderId);
        return ResponseEntity.ok(FileUploadResponse.from(fileInfo));
    }
    
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "一次上传多个文件")
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "上传者ID") @RequestHeader(value = "X-User-Id", required = false) String uploaderId) {
        
        List<FileInfo> fileInfos = fileApplicationService.uploadFiles(files, uploaderId);
        List<FileUploadResponse> responses = fileInfos.stream()
                .map(FileUploadResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{fileId}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件ID") @PathVariable String fileId) {
        
        FileInfo fileInfo = fileApplicationService.getFileInfo(fileId);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new InputStreamResource(fileApplicationService.downloadFile(fileId));
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + fileInfo.getOriginalName() + "\"")
                .body(resource);
    }
    
    @GetMapping("/{fileId}/info")
    @Operation(summary = "获取文件信息", description = "获取文件的元数据信息")
    public ResponseEntity<FileUploadResponse> getFileInfo(
            @Parameter(description = "文件ID") @PathVariable String fileId) {
        
        FileInfo fileInfo = fileApplicationService.getFileInfo(fileId);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(FileUploadResponse.from(fileInfo));
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "文件ID") @PathVariable String fileId) {
        
        fileApplicationService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{fileId}/presigned-url")
    @Operation(summary = "获取预签名URL", description = "获取文件的临时访问URL")
    public ResponseEntity<String> getPresignedUrl(
            @Parameter(description = "文件ID") @PathVariable String fileId,
            @Parameter(description = "过期时间(秒)") @RequestParam(defaultValue = "3600") int expiresInSeconds) {
        
        String url = fileApplicationService.getPresignedUrl(fileId, expiresInSeconds);
        return ResponseEntity.ok(url);
    }
}
