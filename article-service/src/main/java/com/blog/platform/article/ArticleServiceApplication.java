package com.blog.platform.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
    "com.blog.platform.article",
    "com.blog.platform.common"
})
@EnableDiscoveryClient
public class ArticleServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ArticleServiceApplication.class, args);
    }
}