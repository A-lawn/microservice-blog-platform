package com.blog.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * API限流过滤器
 * 基于Redis实现分布式限流
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, String> redisTemplate;
    
    // 限流配置
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int AUTHENTICATED_REQUESTS_PER_MINUTE = 120;
    private static final int ADMIN_REQUESTS_PER_MINUTE = 300;

    public RateLimitingFilter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 获取客户端标识
        String clientId = getClientId(request);
        String userRole = getUserRole(request);
        
        // 根据用户角色确定限流阈值
        int requestLimit = getRequestLimit(userRole);
        
        // 检查限流
        if (isRateLimited(clientId, requestLimit)) {
            return onRateLimitExceeded(exchange);
        }
        
        return chain.filter(exchange);
    }

    private String getClientId(ServerHttpRequest request) {
        // 优先使用用户ID，其次使用IP地址
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }
        
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private String getUserRole(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 这里可以解析JWT获取角色，简化处理
            return request.getHeaders().getFirst("X-User-Role");
        }
        return null;
    }

    private int getRequestLimit(String userRole) {
        if ("ADMIN".equals(userRole)) {
            return ADMIN_REQUESTS_PER_MINUTE;
        } else if (userRole != null) {
            return AUTHENTICATED_REQUESTS_PER_MINUTE;
        } else {
            return DEFAULT_REQUESTS_PER_MINUTE;
        }
    }

    private boolean isRateLimited(String clientId, int requestLimit) {
        String key = "rate_limit:" + clientId;
        String currentMinute = String.valueOf(System.currentTimeMillis() / 60000);
        String rateLimitKey = key + ":" + currentMinute;
        
        try {
            String currentCount = redisTemplate.opsForValue().get(rateLimitKey);
            int count = currentCount != null ? Integer.parseInt(currentCount) : 0;
            
            if (count >= requestLimit) {
                return true;
            }
            
            // 增加计数器
            redisTemplate.opsForValue().increment(rateLimitKey);
            redisTemplate.expire(rateLimitKey, Duration.ofMinutes(1));
            
            return false;
        } catch (Exception e) {
            // Redis异常时不限流，确保服务可用性
            return false;
        }
    }

    private Mono<Void> onRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-RateLimit-Retry-After", "60");

        String errorMessage = "{\"error\":\"Rate limit exceeded\",\"status\":429,\"message\":\"Too many requests, please try again later\"}";
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0; // 在认证之后执行
    }
}