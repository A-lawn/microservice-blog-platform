package com.blog.platform.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
    "com.blog.platform.comment",
    "com.blog.platform.common"
})
@EnableDiscoveryClient
public class CommentServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}