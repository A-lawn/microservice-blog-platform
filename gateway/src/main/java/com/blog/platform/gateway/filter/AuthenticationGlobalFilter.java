package com.blog.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
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
 * 全局JWT认证过滤器
 * 验证请求中的JWT令牌并提取用户信息
 */
@Component
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTokenValidator jwtTokenValidator;

    private static final List<String> OPEN_GET_ENDPOINTS = Arrays.asList(
            "/api/users/register",
            "/api/users/login",
            "/api/articles",
            "/api/categories",
            "/api/tags",
            "/actuator",
            "/fallback"
    );

    private static final List<String> OPEN_POST_ENDPOINTS = Arrays.asList(
            "/api/users/register",
            "/api/users/login"
    );

    public AuthenticationGlobalFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if (isOpenEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtTokenValidator.validateToken(token)) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            String userId = jwtTokenValidator.getUserIdFromToken(token);
            String username = jwtTokenValidator.getUsernameFromToken(token);
            String role = jwtTokenValidator.getRoleFromToken(token);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-User-Role", role != null ? role : "USER")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "JWT token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isOpenEndpoint(String path, String method) {
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return OPEN_GET_ENDPOINTS.stream().anyMatch(path::startsWith);
        }
        if ("POST".equals(method)) {
            return OPEN_POST_ENDPOINTS.stream().anyMatch(path::startsWith);
        }
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String errorMessage = String.format("{\"error\":\"%s\",\"status\":%d}", err, httpStatus.value());
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // 高优先级，首先执行认证
    }
}