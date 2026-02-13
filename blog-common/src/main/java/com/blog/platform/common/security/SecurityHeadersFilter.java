package com.blog.platform.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        String csp = "default-src 'self'; " +
                     "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                     "style-src 'self' 'unsafe-inline'; " +
                     "img-src 'self' data: https:; " +
                     "font-src 'self' data:; " +
                     "connect-src 'self'; " +
                     "frame-ancestors 'none'; " +
                     "base-uri 'self'; " +
                     "form-action 'self'";
        httpResponse.setHeader("Content-Security-Policy", csp);
        
        String origin = httpRequest.getHeader("Origin");
        if (origin != null && isAllowedOrigin(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Methods", 
                "GET, POST, PUT, DELETE, OPTIONS");
            httpResponse.setHeader("Access-Control-Allow-Headers", 
                "Authorization, Content-Type, X-Requested-With, X-XSRF-TOKEN");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
        }
        
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isAllowedOrigin(String origin) {
        return origin.contains("localhost") || 
               origin.contains("127.0.0.1") ||
               origin.endsWith(".blog.com");
    }
}
