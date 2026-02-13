package com.blog.platform.user.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 * Updated for Spring Security 6.x API
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/users/*/profile").authenticated()
                .requestMatchers("/api/users/validate-token").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), 
                           UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
