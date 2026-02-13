# 微服务博客平台 (BlogX)

一个基于 Spring Cloud 微服务架构的现代博客平台，采用 DDD 领域驱动设计，支持文章发布、评论互动、全文搜索等功能。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.3.7 | 基础框架 |
| Spring Cloud | 2023.0.4 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.3.3 | 阿里巴巴微服务组件 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.x | 缓存中间件 |
| Elasticsearch | 8.14.0 | 搜索引擎 |
| Seata | 2.0.0 | 分布式事务 |
| RocketMQ | 2.3.1 | 消息队列 |
| JWT | 0.12.6 | 认证令牌 |
| Flyway | 9.22.3 | 数据库迁移 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vue Router | 4.2.x | 路由管理 |
| Pinia | 2.1.x | 状态管理 |
| Element Plus | 2.4.x | UI 组件库 |
| TypeScript | 5.3.x | 类型支持 |
| Vite | 5.0.x | 构建工具 |
| Axios | 1.6.x | HTTP 客户端 |

## 项目结构

```
microservice-blog-platform/
├── blog-common/                    # 公共模块
│   └── src/main/java/
│       ├── cache/                  # 缓存服务
│       ├── config/                 # 公共配置
│       ├── domain/                 # 领域基础组件
│       ├── exception/              # 异常处理
│       ├── infrastructure/         # 基础设施
│       └── messaging/              # 消息队列
├── user-service/                   # 用户服务 (端口: 8081)
│   └── src/main/java/
│       ├── application/            # 应用层
│       ├── domain/                 # 领域层
│       ├── infrastructure/         # 基础设施层
│       └── interfaces/             # 接口层
├── article-service/                # 文章服务 (端口: 8082)
├── comment-service/                # 评论服务 (端口: 8083)
├── file-service/                   # 文件服务 (端口: 8084)
├── gateway/                        # API 网关 (端口: 8080)
├── blog-frontend/                  # 前端应用 (端口: 3000)
├── seata/                          # Seata 配置
├── docker-compose.yml              # Docker 编排
└── pom.xml                         # 父 POM
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| gateway | 8080 | API 网关 |
| user-service | 8081 | 用户服务 |
| article-service | 8082 | 文章服务 |
| comment-service | 8083 | 评论服务 |
| file-service | 8084 | 文件服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Elasticsearch | 9200 | 搜索引擎 |
| Nacos | 8848 | 服务注册中心 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 7.x

### 启动基础设施

```bash
# 启动 MySQL、Redis、Elasticsearch 等基础设施
docker-compose -f docker-compose.minimal.yml up -d
```

### 启动后端服务

```bash
# 编译项目
mvn clean install -DskipTests

# 启动各服务（按顺序）
cd gateway && mvn spring-boot:run &
cd user-service && mvn spring-boot:run &
cd article-service && mvn spring-boot:run &
cd comment-service && mvn spring-boot:run &
```

### 启动前端

```bash
cd blog-frontend
npm install
npm run dev
```

### 访问应用

- 前端首页: http://localhost:3000
- API 网关: http://localhost:8080
- API 文档: http://localhost:8080/doc.html

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | test123 |

## API 端点

### 用户服务 `/api/users`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/register` | 用户注册 |
| POST | `/login` | 用户登录 |
| POST | `/logout` | 用户登出 |
| GET | `/{userId}` | 获取用户信息 |
| GET | `/profile` | 获取当前用户资料 |
| PUT | `/{userId}/profile` | 更新用户资料 |

### 文章服务 `/api/articles`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/` | 创建文章 |
| PUT | `/{articleId}` | 更新文章 |
| PUT | `/{articleId}/publish` | 发布文章 |
| DELETE | `/{articleId}` | 删除文章 |
| GET | `/{articleId}` | 获取文章详情 |
| GET | `/` | 获取文章列表 |
| GET | `/search` | 搜索文章 |
| POST | `/{articleId}/like` | 点赞文章 |
| POST | `/{articleId}/bookmark` | 收藏文章 |

### 评论服务 `/api/comments`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/` | 创建评论 |
| POST | `/{commentId}/reply` | 回复评论 |
| GET | `/article/{articleId}` | 获取文章评论 |
| GET | `/article/{articleId}/tree` | 获取评论树 |

## 数据库设计

### 用户数据库 (user_db)

- `users` - 用户表
- `user_statistics` - 用户统计表
- `user_roles` - 用户角色表

### 文章数据库 (article_db)

- `articles` - 文章表
- `article_statistics` - 文章统计表
- `categories` - 分类表
- `tags` - 标签表
- `article_tags` - 文章标签关联表
- `article_likes` - 文章点赞表
- `article_bookmarks` - 文章收藏表

### 评论数据库 (comment_db)

- `comments` - 评论表
- `comment_statistics` - 评论统计表

## 架构设计

### DDD 分层架构

```
interfaces/          # 接口层 - REST API
    └── rest/        # REST 控制器
application/         # 应用层 - 用例编排
    └── service/     # 应用服务
domain/              # 领域层 - 业务核心
    ├── model/       # 领域模型
    ├── repository/  # 仓储接口
    └── service/     # 领域服务
infrastructure/      # 基础设施层
    ├── persistence/ # 持久化实现
    ├── messaging/   # 消息队列
    └── config/      # 配置
```

### 微服务架构

```
                    ┌─────────────┐
                    │   前端应用   │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  API 网关   │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼───────┐  ┌───────▼───────┐  ┌───────▼───────┐
│  用户服务     │  │  文章服务     │  │  评论服务     │
│  (8081)       │  │  (8082)       │  │  (8083)       │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
    ┌──────────────────────┼──────────────────────┐
    │                      │                      │
┌───▼───┐            ┌─────▼─────┐          ┌─────▼─────┐
│ MySQL │            │   Redis   │          │    ES     │
└───────┘            └───────────┘          └───────────┘
```

## 功能特性

### 用户管理
- 用户注册/登录
- JWT 认证
- 个人资料管理
- 用户关注系统

### 文章管理
- 文章创建/编辑/删除
- Markdown 支持
- 草稿自动保存
- 文章分类和标签
- 全文搜索
- 文章点赞/收藏

### 评论系统
- 多级评论
- 评论点赞
- 评论审核

### 管理后台
- 用户管理
- 文章管理
- 评论管理
- 分类/标签管理
- 数据统计

## 监控运维

### 健康检查

```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### 日志配置

日志文件位于各服务的 `logs/` 目录下：
- `application.log` - 应用日志
- `error.log` - 错误日志

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一异常处理
- 统一响应格式

### 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## License

MIT License
