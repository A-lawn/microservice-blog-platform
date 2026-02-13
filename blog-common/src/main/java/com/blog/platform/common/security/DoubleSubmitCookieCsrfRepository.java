package com.blog.platform.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class DoubleSubmitCookieCsrfRepository implements CsrfTokenRepository {

    private static final Logger logger = LoggerFactory.getLogger(DoubleSubmitCookieCsrfRepository.class);
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_PARAMETER_NAME = "_csrf";
    private static final int TOKEN_LENGTH = 32;
    
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        setCookie(request, token);
        
        return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, token);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            clearCookie(response);
        } else {
            setCookieHeader(response, token.getToken());
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        String cookieToken = getCookieValue(request, CSRF_COOKIE_NAME);
        if (cookieToken == null) {
            return null;
        }
        return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, cookieToken);
    }
    
    public boolean validateToken(HttpServletRequest request) {
        String cookieToken = getCookieValue(request, CSRF_COOKIE_NAME);
        String headerToken = request.getHeader(CSRF_HEADER_NAME);
        
        if (cookieToken == null || headerToken == null) {
            return false;
        }
        
        return constantTimeEquals(cookieToken, headerToken);
    }
    
    private void setCookie(HttpServletRequest request, String token) {
        if (request.getAttribute(CSRF_COOKIE_NAME) == null) {
            request.setAttribute(CSRF_COOKIE_NAME, token);
        }
    }
    
    private void setCookieHeader(HttpServletResponse response, String token) {
        String cookie = String.format("%s=%s; Path=/; HttpOnly; SameSite=Strict; Secure",
                CSRF_COOKIE_NAME, token);
        response.addHeader("Set-Cookie", cookie);
    }
    
    private void clearCookie(HttpServletResponse response) {
        String cookie = String.format("%s=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict",
                CSRF_COOKIE_NAME);
        response.addHeader("Set-Cookie", cookie);
    }
    
    private String getCookieValue(HttpServletRequest request, String name) {
        String cookies = request.getHeader("Cookie");
        if (cookies == null) {
            return null;
        }
        
        String[] cookiePairs = cookies.split(";");
        for (String pair : cookiePairs) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return keyValue[1];
            }
        }
        return null;
    }
    
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
