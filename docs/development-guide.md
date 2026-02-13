# 开发指南

## 一、环境准备

### 1.1 必需软件

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | Java运行环境 |
| Maven | 3.6+ | 项目构建 |
| Docker | 20.10+ | 容器化部署 |
| Git | 2.30+ | 版本控制 |
| IDE | IntelliJ IDEA 2023+ | 开发工具 |

### 1.2 可选软件

| 软件 | 版本 | 用途 |
|------|------|------|
| MySQL | 8.0+ | 本地数据库 |
| Redis | 7.0+ | 本地缓存 |
| Node.js | 18+ | 前端开发 |

### 1.3 IDE配置

#### IntelliJ IDEA

1. 安装插件：
   - Lombok
   - Spring Boot Assistant
   - JPA Buddy

2. 配置：
   - 启用注解处理：Settings → Build → Compiler → Annotation Processors
   - 设置Java 17：File → Project Structure → Project SDK

## 二、项目结构

```
microservice-blog-platform/
├── blog-common/              # 公共模块
│   └── src/main/java/
│       ├── cache/            # 缓存服务
│       ├── config/           # 公共配置
│       ├── domain/           # 领域模型
│       ├── security/         # 安全组件
│       └── messaging/        # 消息处理
├── user-service/             # 用户服务 (8081)
├── article-service/          # 文章服务 (8082)
├── comment-service/          # 评论服务 (8083)
├── file-service/             # 文件服务 (8084)
├── gateway/                  # API网关 (8080)
├── config/                   # 公共配置文件
├── docs/                     # 文档
├── monitoring/               # 监控配置
└── scripts/                  # 脚本
```

## 三、开发流程

### 3.1 克隆项目

```bash
git clone <repository-url>
cd microservice-blog-platform
```

### 3.2 本地开发环境启动

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis nacos

# 2. 初始化数据库
# 在Navicat中执行 scripts/sql/ 目录下的SQL脚本

# 3. 编译项目
mvn clean install -DskipTests

# 4. 启动服务（按顺序）
# 先启动 user-service
cd user-service && mvn spring-boot:run

# 再启动其他服务
cd article-service && mvn spring-boot:run
cd comment-service && mvn spring-boot:run
cd gateway && mvn spring-boot:run
```

### 3.3 分支管理

```
main        # 主分支，稳定版本
develop     # 开发分支
feature/*   # 功能分支
hotfix/*    # 紧急修复分支
release/*   # 发布分支
```

### 3.4 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## 四、代码规范

### 4.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | UserService |
| 方法名 | 小驼峰 | getUserById |
| 常量 | 全大写下划线 | MAX_SIZE |
| 包名 | 全小写 | com.blog.platform |

### 4.2 注释规范

```java
/**
 * 用户服务类
 * 提供用户的CRUD操作
 * 
 * @author developer
 * @version 1.0.0
 */
public class UserService {
    
    /**
     * 根据ID获取用户
     * 
     * @param userId 用户ID
     * @return 用户对象，不存在返回null
     */
    public User getUserById(String userId) {
        // 实现
    }
}
```

### 4.3 异常处理

```java
// 业务异常
throw new DomainException("USER_NOT_FOUND", "用户不存在");

// 参数校验
if (StringUtils.isEmpty(username)) {
    throw new IllegalArgumentException("用户名不能为空");
}
```

## 五、测试规范

### 5.1 单元测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository repository;
    
    @InjectMocks
    private UserService service;
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // given
        when(repository.save(any())).thenReturn(user);
        
        // when
        User result = service.createUser(dto);
        
        // then
        assertNotNull(result);
        verify(repository).save(any());
    }
}
```

### 5.2 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定服务测试
cd user-service && mvn test

# 生成覆盖率报告
mvn test jacoco:report
```

## 六、API开发

### 6.1 Controller规范

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
```

### 6.2 请求验证

```java
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

## 七、数据库操作

### 7.1 JPA实体

```java
@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "username", nullable = false)
    private String username;
}
```

### 7.2 数据库迁移

使用Flyway管理数据库版本：

```sql
-- V6__Add_user_phone.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
CREATE INDEX idx_phone ON users(phone);
```

## 八、配置管理

### 8.1 配置文件

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_db
    username: root
    password: password
```

### 8.2 环境变量

```bash
export DB_URL=jdbc:mysql://prod-db:3306/user_db
export DB_USERNAME=prod_user
export DB_PASSWORD=prod_password
```

## 九、日志规范

### 9.1 日志级别

| 级别 | 用途 |
|------|------|
| ERROR | 错误信息 |
| WARN | 警告信息 |
| INFO | 关键业务信息 |
| DEBUG | 调试信息 |

### 9.2 日志格式

```java
log.info("User {} logged in successfully", userId);
log.error("Failed to create user: {}", userId, exception);
```

## 十、发布流程

### 10.1 版本号规范

```
主版本.次版本.修订版本
1.0.0 → 1.0.1 → 1.1.0 → 2.0.0
```

### 10.2 发布检查清单

- [ ] 所有测试通过
- [ ] 代码审查完成
- [ ] 文档更新
- [ ] 版本号更新
- [ ] CHANGELOG更新
