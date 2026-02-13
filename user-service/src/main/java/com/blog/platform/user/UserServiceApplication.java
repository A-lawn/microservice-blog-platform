package com.blog.platform.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 用户服务启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.blog.platform.user",
    "com.blog.platform.common"
})
@EnableDiscoveryClient
@EnableTransactionManagement
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}