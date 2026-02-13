<template>
  <div class="main-layout" :data-theme="theme">
    <header class="header glass">
      <div class="header-content container">
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span class="logo-text gradient-text">BlogX</span>
        </div>
        
        <nav class="nav">
          <router-link to="/" class="nav-item">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/articles" class="nav-item">
            <el-icon><Document /></el-icon>
            <span>文章</span>
          </router-link>
          <router-link to="/search" class="nav-item">
            <el-icon><Search /></el-icon>
            <span>探索</span>
          </router-link>
        </nav>
        
        <div class="header-right">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索精彩内容..."
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <button class="theme-toggle" @click="toggleTheme">
            <el-icon v-if="theme === 'dark'"><Sunny /></el-icon>
            <el-icon v-else><Moon /></el-icon>
          </button>
          
          <template v-if="userStore.isLoggedIn">
            <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" class="notification-badge">
              <button class="icon-btn" @click="router.push('/notifications')">
                <el-icon><Bell /></el-icon>
              </button>
            </el-badge>
            
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-avatar">
                <el-avatar :size="36" :src="userStore.user?.avatarUrl">
                  {{ userStore.user?.nickname?.charAt(0) }}
                </el-avatar>
                <span class="status-dot"></span>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <div class="dropdown-header">
                    <el-avatar :size="48" :src="userStore.user?.avatarUrl">
                      {{ userStore.user?.nickname?.charAt(0) }}
                    </el-avatar>
                    <div class="user-info">
                      <span class="nickname">{{ userStore.user?.nickname }}</span>
                      <span class="email">{{ userStore.user?.email }}</span>
                    </div>
                  </div>
                  <el-dropdown-item command="profile" divided>
                    <el-icon><User /></el-icon>个人主页
                  </el-dropdown-item>
                  <el-dropdown-item command="write">
                    <el-icon><Edit /></el-icon>写文章
                  </el-dropdown-item>
                  <el-dropdown-item command="bookmarks">
                    <el-icon><Star /></el-icon>我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>设置
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <button class="btn btn-secondary" @click="router.push('/login')">登录</button>
            <button class="btn btn-primary" @click="router.push('/register')">注册</button>
          </template>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <footer class="footer">
      <div class="footer-content container">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="logo">
              <div class="logo-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
                </svg>
              </div>
              <span class="logo-text gradient-text">BlogX</span>
            </div>
            <p class="brand-desc">分享知识，记录生活，探索无限可能</p>
          </div>
          <div class="footer-links">
            <h4>快速链接</h4>
            <router-link to="/">首页</router-link>
            <router-link to="/articles">文章</router-link>
            <router-link to="/search">探索</router-link>
          </div>
          <div class="footer-links">
            <h4>关于我们</h4>
            <a href="#">关于</a>
            <a href="#">联系</a>
            <a href="#">隐私政策</a>
          </div>
          <div class="footer-links">
            <h4>关注我们</h4>
            <div class="social-links">
              <a href="#" class="social-link"><el-icon><Link /></el-icon></a>
              <a href="#" class="social-link"><el-icon><ChatDotRound /></el-icon></a>
              <a href="#" class="social-link"><el-icon><Message /></el-icon></a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2026 BlogX. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useNotificationStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const searchKeyword = ref('')
const theme = ref(localStorage.getItem('theme') || 'dark')

const toggleTheme = () => {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('theme', theme.value)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push(`/user/${userStore.user?.id}`)
      break
    case 'write':
      router.push('/write')
      break
    case 'bookmarks':
      router.push('/bookmarks')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    notificationStore.fetchUnreadCount()
  }
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 12px 0;
  
  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  
  .logo-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--gradient-primary);
    border-radius: var(--radius-md);
    color: white;
    
    svg {
      width: 20px;
      height: 20px;
    }
  }
  
  .logo-text {
    font-size: 22px;
    font-weight: 700;
    letter-spacing: -0.5px;
  }
}

.nav {
  display: flex;
  gap: 8px;
  
  .nav-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 500;
    border-radius: var(--radius-md);
    transition: all var(--transition-fast);
    
    &:hover {
      color: var(--text-primary);
      background: var(--bg-glass);
    }
    
    &.router-link-active {
      color: var(--primary-color);
      background: rgba(99, 102, 241, 0.1);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .search-box {
    .search-input {
      width: 240px;
    }
  }
  
  .theme-toggle {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-glass);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    color: var(--text-secondary);
    cursor: pointer;
    transition: all var(--transition-fast);
    
    &:hover {
      color: var(--primary-color);
      border-color: var(--primary-color);
    }
  }
  
  .notification-badge {
    .icon-btn {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-glass);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      cursor: pointer;
      transition: all var(--transition-fast);
      
      &:hover {
        color: var(--primary-color);
        border-color: var(--primary-color);
      }
    }
  }
  
  .user-avatar {
    position: relative;
    cursor: pointer;
    
    .status-dot {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 10px;
      height: 10px;
      background: var(--success-color);
      border: 2px solid var(--bg-primary);
      border-radius: 50%;
    }
  }
}

.user-dropdown {
  .dropdown-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    
    .user-info {
      display: flex;
      flex-direction: column;
      
      .nickname {
        font-weight: 600;
        color: var(--text-primary);
      }
      
      .email {
        font-size: 12px;
        color: var(--text-muted);
      }
    }
  }
}

.main-content {
  flex: 1;
  padding: 32px 0;
}

.footer {
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  padding: 48px 0 24px;
  
  .footer-grid {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr;
    gap: 48px;
    margin-bottom: 32px;
  }
  
  .footer-brand {
    .brand-desc {
      color: var(--text-secondary);
      margin-top: 12px;
      font-size: 14px;
    }
  }
  
  .footer-links {
    h4 {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 16px;
      color: var(--text-primary);
    }
    
    a {
      display: block;
      color: var(--text-secondary);
      font-size: 14px;
      margin-bottom: 8px;
      transition: color var(--transition-fast);
      
      &:hover {
        color: var(--primary-color);
      }
    }
    
    .social-links {
      display: flex;
      gap: 12px;
      
      .social-link {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bg-glass);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-md);
        color: var(--text-secondary);
        transition: all var(--transition-fast);
        
        &:hover {
          color: var(--primary-color);
          border-color: var(--primary-color);
          transform: translateY(-2px);
        }
      }
    }
  }
  
  .footer-bottom {
    text-align: center;
    padding-top: 24px;
    border-top: 1px solid var(--border-color);
    
    p {
      color: var(--text-muted);
      font-size: 13px;
    }
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
