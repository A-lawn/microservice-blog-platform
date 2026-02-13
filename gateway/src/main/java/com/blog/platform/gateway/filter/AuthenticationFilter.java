package com.blog.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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
 * JWT认证过滤器
 * 验证请求中的JWT令牌并提取用户信息
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;

    // 不需要认证的路径
    private static final List<String> OPEN_API_ENDPOINTS = Arrays.asList(
            "/api/users/register",
            "/api/users/login",
            "/api/articles",
            "/api/categories",
            "/api/tags",
            "/actuator"
    );

    public AuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 检查是否为开放API
            if (isOpenApiEndpoint(path)) {
                return chain.filter(exchange);
            }

            // 提取Authorization头
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 验证JWT令牌
                if (!jwtTokenValidator.validateToken(token)) {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                // 提取用户信息并添加到请求头
                String userId = jwtTokenValidator.getUserIdFromToken(token);
                String username = jwtTokenValidator.getUsernameFromToken(token);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "JWT token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isOpenApiEndpoint(String path) {
        return OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
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