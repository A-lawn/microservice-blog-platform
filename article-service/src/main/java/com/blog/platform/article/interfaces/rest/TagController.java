package com.blog.platform.article.interfaces.rest;

import com.blog.platform.article.infrastructure.persistence.entity.TagEntity;
import com.blog.platform.article.infrastructure.persistence.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {
    
    @Autowired
    private TagRepository tagRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTags() {
        List<TagEntity> tags = tagRepository.findAllOrderByArticleCountDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TagEntity tag : tags) {
            result.add(convertToMap(tag));
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularTags(
            @RequestParam(defaultValue = "20") int limit) {
        List<TagEntity> tags = tagRepository.findAllOrderByArticleCountDesc(PageRequest.of(0, limit)).getContent();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TagEntity tag : tags) {
            result.add(convertToMap(tag));
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTagById(@PathVariable Long id) {
        return tagRepository.findById(id)
            .map(tag -> ResponseEntity.ok(ApiResponse.success(convertToMap(tag))))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchTags(@RequestParam String keyword) {
        List<TagEntity> tags = tagRepository.searchByName(keyword);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TagEntity tag : tags) {
            result.add(convertToMap(tag));
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTag(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "标签名称不能为空"));
        }
        
        TagEntity tag = new TagEntity();
        tag.setName(name);
        tag.setSlug((String) request.get("slug"));
        tag.setDescription((String) request.get("description"));
        tag.setArticleCount(0);
        tag.setCreatedAt(LocalDateTime.now());
        
        TagEntity saved = tagRepository.save(tag);
        return ResponseEntity.ok(ApiResponse.success("创建成功", convertToMap(saved)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTag(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return tagRepository.findById(id)
            .map(tag -> {
                if (request.get("name") != null) {
                    tag.setName((String) request.get("name"));
                }
                if (request.containsKey("slug")) {
                    tag.setSlug((String) request.get("slug"));
                }
                if (request.containsKey("description")) {
                    tag.setDescription((String) request.get("description"));
                }
                
                TagEntity saved = tagRepository.save(tag);
                return ResponseEntity.ok(ApiResponse.success("更新成功", convertToMap(saved)));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        if (!tagRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        tagRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    private Map<String, Object> convertToMap(TagEntity tag) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tag.getId());
        map.put("name", tag.getName());
        map.put("slug", tag.getSlug());
        map.put("description", tag.getDescription());
        map.put("articleCount", tag.getArticleCount());
        map.put("createdAt", tag.getCreatedAt());
        return map;
    }
}
