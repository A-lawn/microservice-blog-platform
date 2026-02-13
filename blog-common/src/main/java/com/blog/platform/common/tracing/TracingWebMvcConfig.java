package com.blog.platform.common.tracing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TracingWebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private TracingInterceptor tracingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tracingInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/actuator/**", "/error", "/favicon.ico");
    }
}
