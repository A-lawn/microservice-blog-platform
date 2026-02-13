# 本地开发启动指南

## 一、环境准备

### 必需软件
- JDK 17+
- Maven 3.6+
- Docker Desktop

### 验证环境
```powershell
java -version
mvn -version
docker --version
```

## 二、快速启动

### 步骤1：启动基础设施

```powershell
# 进入项目目录
cd e:\workspace_app\microservice-blog-platform

# 启动基础设施服务
docker-compose -f docker-compose.dev.yml up -d
```

等待约30秒，确保所有服务启动完成。

### 步骤2：初始化数据库

```powershell
# 方式一：使用Docker执行SQL
docker exec -i blog-mysql mysql -uroot -proot123 < scripts/sql/00_init_databases.sql
docker exec -i blog-mysql mysql -uroot -proot123 user_db < scripts/sql/02_user_db_tables.sql
docker exec -i blog-mysql mysql -uroot -proot123 article_db < scripts/sql/03_article_db_tables.sql
docker exec -i blog-mysql mysql -uroot -proot123 comment_db < scripts/sql/04_comment_db_tables.sql

# 方式二：使用MySQL客户端工具（如Navicat、DBeaver）
# 连接 localhost:3306，用户root，密码root123
# 依次执行 scripts/sql/ 目录下的SQL文件
```

### 步骤3：编译项目

```powershell
mvn clean install -DskipTests
```

### 步骤4：启动业务服务

打开5个终端窗口，分别执行：

**终端1 - 用户服务：**
```powershell
cd user-service
mvn spring-boot:run
```

**终端2 - 文章服务：**
```powershell
cd article-service
mvn spring-boot:run
```

**终端3 - 评论服务：**
```powerspowershell
cd comment-service
mvn spring-boot:run
```

**终端4 - 文件服务：**
```powershell
cd file-service
mvn spring-boot:run
```

**终端5 - API网关：**
```powershell
cd gateway
mvn spring-boot:run
```

## 三、验证服务

### 检查服务健康
```powershell
# 用户服务
curl http://localhost:8081/actuator/health

# 文章服务
curl http://localhost:8082/actuator/health

# 评论服务
curl http://localhost:8083/actuator/health

# 文件服务
curl http://localhost:8084/actuator/health

# 网关
curl http://localhost:8080/actuator/health
```

### 检查Nacos注册
访问 http://localhost:8848/nacos
- 用户名：nacos
- 密码：nacos

在"服务管理 > 服务列表"中查看已注册的服务。

## 四、服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| API网关 | http://localhost:8080 | 统一入口 |
| 用户服务 | http://localhost:8081 | 用户管理 |
| 文章服务 | http://localhost:8082 | 文章管理 |
| 评论服务 | http://localhost:8083 | 评论管理 |
| 文件服务 | http://localhost:8084 | 文件管理 |
| Nacos | http://localhost:8848/nacos | 服务注册中心 |
| Elasticsearch | http://localhost:9200 | 搜索引擎 |

## 五、常见问题

### Q1: 端口被占用
```powershell
# 查看端口占用
netstat -ano | findstr :8080

# 结束进程
taskkill /PID <进程ID> /F
```

### Q2: MySQL连接失败
```powershell
# 检查MySQL容器状态
docker ps | findstr mysql

# 查看日志
docker logs blog-mysql
```

### Q3: Nacos连接失败
```powershell
# 等待Nacos完全启动（约30秒）
# 检查Nacos日志
docker logs blog-nacos
```

### Q4: 服务注册失败
检查各服务的 application.yml 中 Nacos 地址配置：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

## 六、停止服务

### 停止业务服务
在各终端按 `Ctrl + C`

### 停止基础设施
```powershell
docker-compose -f docker-compose.dev.yml down
```

### 清理数据（可选）
```powershell
docker-compose -f docker-compose.dev.yml down -v
```
