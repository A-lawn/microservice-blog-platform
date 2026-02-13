# API 接口文档

## 一、文档访问地址

启动服务后，可通过以下地址访问 Knife4j 接口文档：

| 服务 | 地址 | 说明 |
|------|------|------|
| 用户服务 | http://localhost:8081/doc.html | 用户注册、登录、个人信息管理 |
| 文章服务 | http://localhost:8082/doc.html | 文章CRUD、分类、标签管理 |
| 评论服务 | http://localhost:8083/doc.html | 评论、回复、点赞管理 |
| 网关聚合 | http://localhost:8080/doc.html | 所有服务的聚合文档 |

## 二、认证说明

### 2.1 获取 Token

```http
POST /api/users/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000
  }
}
```

### 2.2 使用 Token

在请求头中添加 Authorization：
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 三、接口列表

### 3.1 用户服务 (user-service)

#### 用户管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/users/register | 用户注册 | 否 |
| POST | /api/users/login | 用户登录 | 否 |
| GET | /api/users/{id} | 获取用户信息 | 是 |
| PUT | /api/users/{id} | 更新用户信息 | 是 |
| DELETE | /api/users/{id} | 删除用户 | 是 |
| GET | /api/users/profile | 获取当前用户信息 | 是 |

#### 用户关注

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/users/{userId}/follow | 关注用户 | 是 |
| DELETE | /api/users/{userId}/follow | 取消关注 | 是 |
| GET | /api/users/{userId}/followers | 获取粉丝列表 | 是 |
| GET | /api/users/{userId}/following | 获取关注列表 | 是 |

#### 通知管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/notifications | 获取通知列表 | 是 |
| GET | /api/notifications/unread-count | 获取未读数量 | 是 |
| PUT | /api/notifications/{id}/read | 标记已读 | 是 |
| PUT | /api/notifications/read-all | 全部标记已读 | 是 |

### 3.2 文章服务 (article-service)

#### 文章管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/articles | 创建文章 | 是 |
| GET | /api/articles | 获取文章列表 | 否 |
| GET | /api/articles/{id} | 获取文章详情 | 否 |
| PUT | /api/articles/{id} | 更新文章 | 是 |
| DELETE | /api/articles/{id} | 删除文章 | 是 |
| POST | /api/articles/{id}/publish | 发布文章 | 是 |
| POST | /api/articles/{id}/archive | 归档文章 | 是 |

#### 文章互动

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/articles/{id}/like | 点赞文章 | 是 |
| DELETE | /api/articles/{id}/like | 取消点赞 | 是 |
| POST | /api/articles/{id}/bookmark | 收藏文章 | 是 |
| DELETE | /api/articles/{id}/bookmark | 取消收藏 | 是 |
| GET | /api/articles/{id}/like-status | 获取点赞状态 | 是 |

#### 分类管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/categories | 获取分类列表 | 否 |
| POST | /api/categories | 创建分类 | 是(管理员) |
| PUT | /api/categories/{id} | 更新分类 | 是(管理员) |
| DELETE | /api/categories/{id} | 删除分类 | 是(管理员) |

#### 标签管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/tags | 获取标签列表 | 否 |
| GET | /api/tags/popular | 获取热门标签 | 否 |
| POST | /api/tags | 创建标签 | 是 |

### 3.3 评论服务 (comment-service)

#### 评论管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/comments | 发表评论 | 是 |
| GET | /api/comments/article/{articleId} | 获取文章评论 | 否 |
| GET | /api/comments/{id}/replies | 获取评论回复 | 否 |
| DELETE | /api/comments/{id} | 删除评论 | 是 |

#### 评论互动

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/comments/{id}/like | 点赞评论 | 是 |
| DELETE | /api/comments/{id}/like | 取消点赞 | 是 |
| POST | /api/comments/{id}/report | 举报评论 | 是 |

## 四、通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1707748800000
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": 1707748800000,
  "errors": [
    {
      "field": "username",
      "message": "用户名不能为空"
    }
  ]
}
```

### 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  },
  "timestamp": 1707748800000
}
```

## 五、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

## 六、请求限流

| 服务 | 限流策略 |
|------|----------|
| 用户服务 | 100次/分钟/IP |
| 文章服务 | 200次/分钟/IP |
| 评论服务 | 100次/分钟/IP |
| 网关 | 500次/分钟/IP |

## 七、示例代码

### Java (OkHttp)

```java
OkHttpClient client = new OkHttpClient();

// 登录获取Token
String json = "{\"username\":\"admin\",\"password\":\"123456\"}";
RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
Request request = new Request.Builder()
    .url("http://localhost:8080/api/users/login")
    .post(body)
    .build();
Response response = client.newCall(request).execute();

// 使用Token访问
Request authRequest = new Request.Builder()
    .url("http://localhost:8080/api/users/profile")
    .header("Authorization", "Bearer " + token)
    .build();
```

### JavaScript (Axios)

```javascript
// 登录
const loginResponse = await axios.post('http://localhost:8080/api/users/login', {
  username: 'admin',
  password: '123456'
});
const token = loginResponse.data.data.token;

// 使用Token访问
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
const profile = await axios.get('http://localhost:8080/api/users/profile');
```

### Python (Requests)

```python
import requests

# 登录
response = requests.post('http://localhost:8080/api/users/login', json={
    'username': 'admin',
    'password': '123456'
})
token = response.json()['data']['token']

# 使用Token访问
headers = {'Authorization': f'Bearer {token}'}
profile = requests.get('http://localhost:8080/api/users/profile', headers=headers)
```
