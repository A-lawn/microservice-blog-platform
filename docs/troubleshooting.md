# 故障排查指南

## 一、常见问题

### 1.1 服务启动失败

#### 问题：端口被占用

```
Error: Port 8080 already in use
```

**解决方案**：

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F

# Linux/Mac
lsof -i :8080
kill -9 <pid>
```

#### 问题：数据库连接失败

```
Error: Could not connect to MySQL
```

**排查步骤**：

1. 检查MySQL是否启动
```bash
docker ps | grep mysql
```

2. 检查连接配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_db
    username: root
    password: password
```

3. 检查网络连通性
```bash
telnet localhost 3306
```

#### 问题：Nacos连接失败

```
Error: Request nacos server failed
```

**解决方案**：

1. 检查Nacos状态
```bash
curl http://localhost:8848/nacos/v1/console/health
```

2. 检查配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

### 1.2 服务调用失败

#### 问题：服务发现失败

```
Error: No instances available for user-service
```

**排查步骤**：

1. 检查服务注册状态
```bash
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service
```

2. 检查服务是否启动
3. 检查Nacos配置

#### 问题：网关路由失败

```
Error: 404 Not Found
```

**排查步骤**：

1. 检查路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
```

2. 检查目标服务状态

### 1.3 数据库问题

#### 问题：连接池耗尽

```
Error: HikariPool - Connection is not available
```

**解决方案**：

1. 增加连接池大小
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
```

2. 检查连接泄漏
```yaml
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000
```

#### 问题：慢查询

**排查步骤**：

1. 开启慢查询日志
```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

2. 分析执行计划
```sql
EXPLAIN SELECT * FROM articles WHERE status = 'PUBLISHED';
```

### 1.4 缓存问题

#### 问题：Redis连接失败

```
Error: Unable to connect to Redis
```

**解决方案**：

1. 检查Redis状态
```bash
redis-cli ping
```

2. 检查配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

#### 问题：缓存穿透

**解决方案**：

1. 使用空值缓存
2. 使用布隆过滤器

### 1.5 消息队列问题

#### 问题：RocketMQ连接失败

```
Error: Connect to rocketmq failed
```

**解决方案**：

1. 检查RocketMQ状态
```bash
# 检查NameServer
netstat -an | grep 9876

# 检查Broker
netstat -an | grep 10911
```

2. 检查配置
```yaml
rocketmq:
  name-server: localhost:9876
```

## 二、性能问题排查

### 2.1 CPU使用率高

**排查步骤**：

1. 找出高CPU进程
```bash
# Linux
top -p <pid>

# 查看线程
top -H -p <pid>
```

2. 生成线程转储
```bash
jstack <pid> > thread_dump.txt
```

3. 分析热点方法
```bash
jmap -histo <pid> | head -20
```

### 2.2 内存泄漏

**排查步骤**：

1. 生成堆转储
```bash
jmap -dump:format=b,file=heap.hprof <pid>
```

2. 使用MAT分析

### 2.3 响应慢

**排查步骤**：

1. 检查日志
2. 使用链路追踪
3. 分析数据库慢查询

## 三、日志分析

### 3.1 查看服务日志

```bash
# Docker日志
docker logs <container_id> -f --tail 100

# 文件日志
tail -f logs/user-service.log
```

### 3.2 关键日志搜索

```bash
# 搜索错误日志
grep "ERROR" logs/*.log

# 搜索特定请求
grep "trace-id-xxx" logs/*.log
```

## 四、健康检查

### 4.1 服务健康检查

```bash
# Actuator健康检查
curl http://localhost:8081/actuator/health

# 详细信息
curl http://localhost:8081/actuator/health?show-details=always
```

### 4.2 组件健康检查

| 组件 | 检查命令 |
|------|----------|
| MySQL | `mysql -u root -p -e "SELECT 1"` |
| Redis | `redis-cli ping` |
| Nacos | `curl localhost:8848/nacos/v1/console/health` |
| Elasticsearch | `curl localhost:9200/_cluster/health` |

## 五、应急处理

### 5.1 服务熔断

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

### 5.2 限流降级

```yaml
resilience4j:
  ratelimiter:
    instances:
      default:
        limit-for-period: 100
        limit-refresh-period: 1s
```

### 5.3 服务重启

```bash
# Docker重启
docker restart <container_name>

# Kubernetes重启
kubectl rollout restart deployment/<deployment_name>
```

## 六、监控告警

### 6.1 Prometheus指标

```bash
# 查看指标
curl http://localhost:8081/actuator/prometheus
```

### 6.2 Grafana仪表盘

- JVM监控
- 数据库连接池
- HTTP请求统计
- 业务指标

## 七、联系支持

如遇到无法解决的问题：

1. 收集以下信息：
   - 错误日志
   - 配置文件
   - 环境信息
   - 复现步骤

2. 提交Issue到项目仓库

3. 联系技术支持团队
