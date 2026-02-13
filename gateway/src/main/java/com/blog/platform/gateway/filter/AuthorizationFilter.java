package com.blog.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 授权过滤器
 * 基于用户角色和请求路径进行权限验证
 */
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;

    // 需要管理员权限的路径
    private static final List<String> ADMIN_ENDPOINTS = Arrays.asList(
            "/api/users/admin",
            "/api/articles/admin",
            "/api/comments/admin"
    );

    // 只读操作的路径（GET请求）
    private static final List<String> READ_ONLY_ENDPOINTS = Arrays.asList(
            "/api/articles",
            "/api/comments"
    );

    public AuthorizationFilter(JwtTokenValidator jwtTokenValidator) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            HttpMethod method = request.getMethod();

            // 获取用户信息
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange); // 认证过滤器会处理这种情况
            }

            String token = authHeader.substring(7);
            String userRole = jwtTokenValidator.getRoleFromToken(token);

            // 检查管理员权限
            if (isAdminEndpoint(path) && !"ADMIN".equals(userRole)) {
                return onError(exchange, "Insufficient privileges", HttpStatus.FORBIDDEN);
            }

            // 检查写操作权限
            if (isWriteOperation(method) && !isReadOnlyEndpoint(path) && 
                !"USER".equals(userRole) && !"ADMIN".equals(userRole)) {
                return onError(exchange, "Insufficient privileges for write operation", HttpStatus.FORBIDDEN);
            }

            return chain.filter(exchange);
        };
    }

    private boolean isAdminEndpoint(String path) {
        return ADMIN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isReadOnlyEndpoint(String path) {
        return READ_ONLY_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isWriteOperation(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || 
               method == HttpMethod.DELETE || method == HttpMethod.PATCH;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String errorMessage = String.format("{\"error\":\"%s\",\"status\":%d}", err, httpStatus.value());
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 配置属性可以在这里添加
    }
}