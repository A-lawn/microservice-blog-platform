package com.blog.platform.common.repository;

import com.blog.platform.common.domain.AggregateRoot;

import java.util.List;
import java.util.Optional;

/**
 * 通用Repository接口
 * 定义了基本的CRUD操作
 */
public interface Repository<T extends AggregateRoot<ID>, ID> {
    
    /**
     * 保存聚合根
     */
    T save(T aggregate);
    
    /**
     * 根据ID查找聚合根
     */
    Optional<T> findById(ID id);
    
    /**
     * 检查聚合根是否存在
     */
    boolean existsById(ID id);
    
    /**
     * 删除聚合根
     */
    void delete(T aggregate);
    
    /**
     * 根据ID删除聚合根
     */
    void deleteById(ID id);
    
    /**
     * 查找所有聚合根
     */
    List<T> findAll();
    
    /**
     * 获取聚合根总数
     */
    long count();
}