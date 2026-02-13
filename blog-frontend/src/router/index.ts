import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'articles',
        name: 'Articles',
        component: () => import('@/views/article/list.vue'),
        meta: { title: '文章列表' },
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/article/detail.vue'),
        meta: { title: '文章详情' },
      },
      {
        path: 'write',
        name: 'WriteArticle',
        component: () => import('@/views/article/write.vue'),
        meta: { title: '写文章', requiresAuth: true },
      },
      {
        path: 'edit/:id',
        name: 'EditArticle',
        component: () => import('@/views/article/write.vue'),
        meta: { title: '编辑文章', requiresAuth: true },
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/search/index.vue'),
        meta: { title: '搜索' },
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '用户主页' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/user/settings.vue'),
        meta: { title: '个人设置', requiresAuth: true },
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/notification/index.vue'),
        meta: { title: '消息通知', requiresAuth: true },
      },
      {
        path: 'bookmarks',
        name: 'Bookmarks',
        component: () => import('@/views/article/bookmarks.vue'),
        meta: { title: '我的收藏', requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard.vue'),
        meta: { title: '管理后台' },
      },
      {
        path: 'articles',
        name: 'AdminArticles',
        component: () => import('@/views/admin/articles.vue'),
        meta: { title: '文章管理' },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/comments.vue'),
        meta: { title: '评论管理' },
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/categories.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: () => import('@/views/admin/tags.vue'),
        meta: { title: '标签管理' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title || '博客平台'} - 博客平台`

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
    return
  }

  if (userStore.token && !userStore.user) {
    await userStore.fetchCurrentUser()
  }

  next()
})

export default router
