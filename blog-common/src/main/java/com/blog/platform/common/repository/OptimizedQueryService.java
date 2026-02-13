package com.blog.platform.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 优化查询服务
 * 提供高性能的数据库查询方法
 */
@Service
public class OptimizedQueryService {
    
    private final JdbcTemplate jdbcTemplate;
    
    public OptimizedQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 执行优化的分页查询
     * 使用LIMIT OFFSET优化，避免深度分页性能问题
     */
    public <T> Page<T> executeOptimizedPageQuery(String sql, Object[] params, 
                                                Pageable pageable, RowMapper<T> rowMapper) {
        // 构建计数查询
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") as count_query";
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        
        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // 构建分页查询
        String pageSql = sql + " LIMIT ? OFFSET ?";
        Object[] pageParams = new Object[params.length + 2];
        System.arraycopy(params, 0, pageParams, 0, params.length);
        pageParams[params.length] = pageable.getPageSize();
        pageParams[params.length + 1] = pageable.getOffset();
        
        List<T> content = jdbcTemplate.query(pageSql, pageParams, rowMapper);
        
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * 执行游标分页查询（适用于大数据量场景）
     * 使用ID游标避免OFFSET性能问题
     */
    public <T> List<T> executeCursorPageQuery(String sql, Object[] params, 
                                            String lastId, int pageSize, RowMapper<T> rowMapper) {
        String cursorSql;
        Object[] cursorParams;
        
        if (lastId != null && !lastId.isEmpty()) {
            cursorSql = sql + " AND id > ? ORDER BY id LIMIT ?";
            cursorParams = new Object[params.length + 2];
            System.arraycopy(params, 0, cursorParams, 0, params.length);
            cursorParams[params.length] = lastId;
            cursorParams[params.length + 1] = pageSize;
        } else {
            cursorSql = sql + " ORDER BY id LIMIT ?";
            cursorParams = new Object[params.length + 1];
            System.arraycopy(params, 0, cursorParams, 0, params.length);
            cursorParams[params.length] = pageSize;
        }
        
        return jdbcTemplate.query(cursorSql, cursorParams, rowMapper);
    }
    
    /**
     * 批量插入优化
     */
    public int[] batchInsert(String sql, List<Object[]> batchParams) {
        return jdbcTemplate.batchUpdate(sql, batchParams);
    }
    
    /**
     * 批量更新优化
     */
    public int[] batchUpdate(String sql, List<Object[]> batchParams) {
        return jdbcTemplate.batchUpdate(sql, batchParams);
    }
    
    /**
     * 执行只读查询（可以路由到读库）
     */
    public <T> List<T> executeReadOnlyQuery(String sql, Object[] params, RowMapper<T> rowMapper) {
        // 这里可以实现读写分离逻辑
        // 在实际实现中，可以根据配置将查询路由到只读数据源
        return jdbcTemplate.query(sql, params, rowMapper);
    }
    
    /**
     * 执行聚合查询优化
     */
    public <T> T executeAggregateQuery(String sql, Object[] params, Class<T> requiredType) {
        return jdbcTemplate.queryForObject(sql, params, requiredType);
    }
    
    /**
     * 执行EXISTS查询优化
     */
    public boolean executeExistsQuery(String sql, Object[] params) {
        String existsSql = "SELECT EXISTS(" + sql + ")";
        Boolean result = jdbcTemplate.queryForObject(existsSql, params, Boolean.class);
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * 执行IN查询优化（处理大量IN参数）
     */
    public <T> List<T> executeInQuery(String sqlTemplate, String inClause, 
                                    List<Object> inParams, Object[] otherParams, 
                                    RowMapper<T> rowMapper) {
        String sql = sqlTemplate.replace("{IN_CLAUSE}", inClause);
        
        Object[] allParams = new Object[otherParams.length + inParams.size()];
        System.arraycopy(otherParams, 0, allParams, 0, otherParams.length);
        for (int i = 0; i < inParams.size(); i++) {
            allParams[otherParams.length + i] = inParams.get(i);
        }
        
        return jdbcTemplate.query(sql, allParams, rowMapper);
    }
    
    /**
     * 构建IN子句
     */
    public String buildInClause(int paramCount) {
        if (paramCount <= 0) {
            return "()";
        }
        
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        
        return sb.toString();
    }
}