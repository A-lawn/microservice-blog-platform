package com.blog.platform.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Gateway应用程序启动测试
 */
@SpringBootTest
@ActiveProfiles("test")
class GatewayApplicationTest {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }
}