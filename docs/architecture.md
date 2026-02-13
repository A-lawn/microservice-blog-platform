# 企业内容中心 - 架构说明（标准版）

## 1. 项目概览

### 1.1 背景与目标

本项目定位为 **企业内容中心 / 运营内容管理平台**，面向运营、市场、产品团队，主要解决：

- **多类型内容统一管理**：公告、活动、产品更新、帮助文档等运营内容的创建、审核与发布。
- **多渠道发布与展示**：内容可按频道/位置投放到不同前端（官网、活动页、APP 内嵌页等）。
- **用户互动与反馈**：评论、回复等用户反馈能力。
- **基础运营分析**：内容发布量、阅读量、互动量等指标，辅助业务决策。

当前默认采用 **标准版（Phase 2）** 架构：微服务拆分 + 网关 + Nacos + 适度 MQ，**不使用分布式事务（Seata）**，跨服务一致性通过本地事务与事件驱动实现。

### 1.2 架构版本说明

| 版本     | 说明 |
|----------|------|
| **标准版**（当前默认） | 用户/内容/反馈服务 + 网关 + Nacos，本地事务 + RocketMQ 异步事件，CQRS/ES 可选关闭。 |
| **高级版**（可选）   | 在标准版基础上开启 `feature.seata.enabled=true`、`feature.cqrs.enabled=true`，启用 Seata 与 CQRS 读模型。 |

---

## 2. 整体架构

### 2.1 服务划分

- **账号与权限服务（user-service）**：运营/用户账号、角色权限（ADMIN/OPERATOR/USER）、注册登录、资料维护。
- **内容服务（article-service）**：内容 CRUD、多状态流转（草稿→待审核→已发布→下线）、列表与搜索（DB 或 ES）。
- **反馈服务（comment-service）**：评论与回复、树形结构、审核与屏蔽。
- **统一接入层（gateway）**：路由、鉴权、限流、熔断。
- **内容域与基础设施模块（blog-common）**：共享领域模型（User/Article/Comment）、领域事件、通用仓储与工具。

### 2.2 技术栈（标准版）

- **框架**：Spring Boot、Spring Cloud Gateway、Spring Cloud Alibaba（Nacos）。
- **存储**：MySQL、Redis；Elasticsearch 为可选项（`feature.cqrs.enabled=true` 时使用）。
- **消息**：RocketMQ（内容/评论领域事件，异步通知与统计）。
- **可观测**：Actuator、Micrometer、Prometheus。
- **容器**：Docker / Docker Compose。

### 2.3 特性开关（application.yml）

标准版默认配置：

```yaml
feature:
  seata.enabled: false   # 关闭分布式事务，使用本地事务 + 事件
  cqrs.enabled: false   # 关闭 CQRS/ES 读模型同步，查询直连 DB
  rocketmq.enabled: true # 适度使用 MQ 发送领域事件
```

---

## 3. 领域与数据

### 3.1 核心领域

- **内容（Article）**：标题、正文、类型、频道、状态、发布时间、统计信息；行为：创建草稿、提交审核、发布、下线、归档。
- **用户（User）**：账号、角色、状态、资料；行为：注册、更新资料、修改密码、角色变更。
- **评论（Comment）**：所属内容、作者、父评论、层级、状态；行为：创建、回复、删除、审核、隐藏。

### 3.2 角色与权限

- **ADMIN**：账号与角色管理、内容审核/下线、评论策略。
- **OPERATOR**：内容创建与编辑、提交审核、查看运营数据。
- **USER**：浏览内容、发表评论（受风控/审核限制）。

### 3.3 数据库与持久化

- MySQL：按服务拆分逻辑库（用户库、内容库、评论库），标准版下各服务仅使用本地事务。
- Redis：会话、热点内容缓存、限流等。
- 持久化层：JPA 为主；可按需在部分场景使用 MyBatis。

---

## 4. 交互与一致性（标准版）

### 4.1 同步调用

- 网关统一暴露 `/api/...`，服务间通过 REST/OpenFeign 同步查询（如内容服务查用户信息）。

### 4.2 异步事件（RocketMQ）

- 内容发布、评论创建等发布领域事件，由 MQ 消费端做通知、统计更新等，**不要求强一致**，最终一致即可。

### 4.3 无 Seata 时的发布/评论流程

- **内容发布**：内容服务内 `article.publish()` + `save()`，发布领域事件到 MQ；不跨库全局事务。
- **评论创建/回复**：评论服务内保存评论并发布事件；文章评论数等由消费端异步更新，标准版不跑 Saga。

---

## 5. 可观测与运维

- **指标**：Actuator + Micrometer，暴露 HTTP、JVM、业务指标，由 Prometheus 抓取。
- **日志**：统一格式，可对接公司日志平台。
- **健康检查**：各服务提供 `/actuator/health`，便于网关与编排健康探测。

---

## 6. 演进与取舍

- **标准版**：优先可运维、可理解，避免引入 Seata/全量 CQRS 的复杂度。
- **高级版**：需要跨服务强一致或复杂读模型时，通过特性开关启用 Seata 与 CQRS，代码路径已预留（Saga、读模型同步）。

如需启用高级版，在对应服务中设置：

```yaml
feature.seata.enabled: true
feature.cqrs.enabled: true  # 仅 article-service
```

并确保 Seata Server、Elasticsearch 等依赖已部署并配置正确。
