# 博客平台前端

基于 Vue 3 + TypeScript + Vite 的博客平台前端项目。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - JavaScript 的超集
- **Vite** - 下一代前端构建工具
- **Vue Router** - Vue.js 官方路由
- **Pinia** - Vue 状态管理
- **Element Plus** - Vue 3 UI 组件库
- **Axios** - HTTP 客户端
- **Marked** - Markdown 解析器
- **DOMPurify** - XSS 防护

## 项目结构

```
blog-frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口
│   │   ├── user.ts        # 用户接口
│   │   ├── article.ts     # 文章接口
│   │   ├── comment.ts     # 评论接口
│   │   ├── notification.ts # 通知接口
│   │   └── file.ts        # 文件接口
│   ├── components/        # 公共组件
│   │   └── ArticleCard.vue
│   ├── layouts/           # 布局组件
│   │   ├── MainLayout.vue # 主布局
│   │   └── AdminLayout.vue # 管理后台布局
│   ├── router/            # 路由配置
│   ├── stores/            # 状态管理
│   │   ├── user.ts        # 用户状态
│   │   ├── article.ts     # 文章状态
│   │   └── notification.ts # 通知状态
│   ├── styles/            # 样式文件
│   ├── types/             # TypeScript 类型
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
│   │   ├── admin/         # 管理后台
│   │   ├── article/       # 文章页面
│   │   ├── auth/          # 认证页面
│   │   ├── home/          # 首页
│   │   ├── notification/  # 通知页面
│   │   ├── search/        # 搜索页面
│   │   └── user/          # 用户页面
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── index.html             # HTML 模板
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript 配置
└── vite.config.ts         # Vite 配置
```

## 功能模块

### 用户模块
- 用户注册/登录
- 个人信息管理
- 用户主页
- 关注/粉丝

### 文章模块
- 文章列表
- 文章详情
- 文章发布/编辑
- 文章搜索
- 点赞/收藏

### 评论模块
- 评论列表
- 发表评论
- 评论回复
- 评论点赞

### 管理后台
- 数据概览
- 文章管理
- 用户管理
- 分类管理
- 标签管理

## 开发指南

### 安装依赖

```bash
cd blog-frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 代码检查

```bash
npm run lint
```

### 代码格式化

```bash
npm run format
```

## 环境配置

### 开发环境

项目默认代理到 `http://localhost:8080` 的后端服务。

### 生产环境

构建时需要配置后端 API 地址。

## 页面路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 最新文章列表 |
| `/articles` | 文章列表 | 所有文章 |
| `/article/:id` | 文章详情 | 文章内容 |
| `/write` | 写文章 | 发布新文章 |
| `/edit/:id` | 编辑文章 | 编辑已有文章 |
| `/search` | 搜索 | 搜索文章 |
| `/login` | 登录 | 用户登录 |
| `/register` | 注册 | 用户注册 |
| `/user/:id` | 用户主页 | 用户信息 |
| `/settings` | 设置 | 个人设置 |
| `/notifications` | 通知 | 消息通知 |
| `/bookmarks` | 收藏 | 我的收藏 |
| `/admin` | 管理后台 | 数据概览 |
| `/admin/articles` | 文章管理 | 管理文章 |
| `/admin/users` | 用户管理 | 管理用户 |
| `/admin/categories` | 分类管理 | 管理分类 |
| `/admin/tags` | 标签管理 | 管理标签 |

## 注意事项

1. **XSS 防护**：所有用户输入的内容都会经过 DOMPurify 过滤
2. **CSRF 防护**：请求自动携带 CSRF Token
3. **认证机制**：使用 JWT Token 进行认证
4. **路由守卫**：需要登录的页面会自动跳转到登录页
