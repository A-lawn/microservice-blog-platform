package com.blog.platform.article.interfaces.rest;

import com.blog.platform.article.infrastructure.persistence.entity.CategoryEntity;
import com.blog.platform.article.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private CategoryJpaRepository categoryRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllCategories() {
        List<CategoryEntity> categories = categoryRepository.findAllByOrderBySortOrder();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (CategoryEntity category : categories) {
            result.add(convertToMap(category));
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .map(category -> ResponseEntity.ok(ApiResponse.success(convertToMap(category))))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategoryTree() {
        List<CategoryEntity> rootCategories = categoryRepository.findByParentIdIsNullOrderBySortOrder();
        List<Map<String, Object>> result = buildCategoryTree(rootCategories);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(@RequestBody Map<String, Object> request) {
        CategoryEntity category = new CategoryEntity();
        category.setName((String) request.get("name"));
        category.setDescription((String) request.get("description"));
        category.setParentId(request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null);
        category.setSortOrder(request.get("sortOrder") != null ? Integer.valueOf(request.get("sortOrder").toString()) : 0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        CategoryEntity saved = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success("创建成功", convertToMap(saved)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return categoryRepository.findById(id)
            .map(category -> {
                if (request.get("name") != null) {
                    category.setName((String) request.get("name"));
                }
                if (request.get("description") != null) {
                    category.setDescription((String) request.get("description"));
                }
                if (request.containsKey("parentId")) {
                    category.setParentId(request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null);
                }
                if (request.get("sortOrder") != null) {
                    category.setSortOrder(Integer.valueOf(request.get("sortOrder").toString()));
                }
                category.setUpdatedAt(LocalDateTime.now());
                
                CategoryEntity saved = categoryRepository.save(category);
                return ResponseEntity.ok(ApiResponse.success("更新成功", convertToMap(saved)));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        List<CategoryEntity> children = categoryRepository.findByParentIdOrderBySortOrder(id);
        if (!children.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "该分类下存在子分类，无法删除"));
        }
        
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    private List<Map<String, Object>> buildCategoryTree(List<CategoryEntity> categories) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (CategoryEntity category : categories) {
            Map<String, Object> categoryMap = convertToMap(category);
            
            List<CategoryEntity> children = categoryRepository.findByParentIdOrderBySortOrder(category.getId());
            if (!children.isEmpty()) {
                categoryMap.put("children", buildCategoryTree(children));
            }
            
            result.add(categoryMap);
        }
        
        return result;
    }
    
    private Map<String, Object> convertToMap(CategoryEntity category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("description", category.getDescription());
        map.put("parentId", category.getParentId());
        map.put("sortOrder", category.getSortOrder());
        map.put("createdAt", category.getCreatedAt());
        return map;
    }
}
