<template>
  <div class="admin-layout" :class="{ 'dark-mode': isDarkMode }">
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <span class="logo-text">管理后台</span>
      </div>
      
      <nav class="nav-menu">
        <router-link to="/admin" class="nav-item" :class="{ active: route.path === '/admin' }">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
          </div>
          <span>数据概览</span>
        </router-link>
        
        <router-link to="/admin/articles" class="nav-item" :class="{ active: route.path.startsWith('/admin/articles') }">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>
            </svg>
          </div>
          <span>文章管理</span>
        </router-link>
        
        <router-link to="/admin/users" class="nav-item" :class="{ active: route.path.startsWith('/admin/users') }">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <span>用户管理</span>
        </router-link>
        
        <router-link to="/admin/comments" class="nav-item" :class="{ active: route.path.startsWith('/admin/comments') }">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <span>评论管理</span>
        </router-link>
        
        <router-link to="/admin/categories" class="nav-item" :class="{ active: route.path.startsWith('/admin/categories') }">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <span>分类管理</span>
        </router-link>
        
        <router-link to="/admin/tags" class="nav-item" :class="{ active: route.path.startsWith('/admin/tags') }">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
              <line x1="7" y1="7" x2="7.01" y2="7"/>
            </svg>
          </div>
          <span>标签管理</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <button class="theme-toggle" @click="toggleTheme">
          <svg v-if="isDarkMode" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/>
            <line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
            <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/>
            <line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
            <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
          </svg>
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="top-bar">
        <div class="breadcrumb">
          <span class="breadcrumb-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </span>
          <span>{{ route.meta.title || '首页' }}</span>
        </div>
        
        <div class="top-actions">
          <button class="action-btn" @click="router.push('/')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
              <polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
            <span>返回前台</span>
          </button>
          
          <div class="user-info">
            <div class="user-avatar">{{ userStore.user?.username?.charAt(0)?.toUpperCase() || 'A' }}</div>
            <span class="user-name">{{ userStore.user?.username || 'Admin' }}</span>
          </div>
        </div>
      </header>
      
      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isDarkMode = ref(false)

onMounted(() => {
  const savedTheme = localStorage.getItem('admin-theme')
  isDarkMode.value = savedTheme === 'dark'
})

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('admin-theme', isDarkMode.value ? 'dark' : 'light')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  
  &.dark-mode {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  }
}

.sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
  
  .dark-mode & {
    background: rgba(26, 26, 46, 0.95);
    border-right-color: rgba(255, 255, 255, 0.05);
  }
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  
  .dark-mode & {
    border-bottom-color: rgba(255, 255, 255, 0.05);
  }
  
  .logo-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    
    svg {
      width: 24px;
      height: 24px;
    }
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  color: #64748b;
  text-decoration: none;
  margin-bottom: 4px;
  transition: all 0.3s ease;
  
  .dark-mode & {
    color: #94a3b8;
  }
  
  .nav-icon {
    width: 20px;
    height: 20px;
    opacity: 0.7;
    
    svg {
      width: 100%;
      height: 100%;
    }
  }
  
  &:hover {
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
    
    .dark-mode & {
      background: rgba(102, 126, 234, 0.2);
    }
  }
  
  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
    
    .nav-icon {
      opacity: 1;
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  
  .dark-mode & {
    border-top-color: rgba(255, 255, 255, 0.05);
  }
}

.theme-toggle {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.05);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  .dark-mode & {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
  
  svg {
    width: 20px;
    height: 20px;
  }
  
  &:hover {
    background: rgba(102, 126, 234, 0.2);
  }
}

.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.top-bar {
  height: 70px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 50;
  
  .dark-mode & {
    background: rgba(26, 26, 46, 0.8);
    border-bottom-color: rgba(255, 255, 255, 0.05);
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  
  .dark-mode & {
    color: #f1f5f9;
  }
  
  .breadcrumb-icon {
    width: 20px;
    height: 20px;
    color: #667eea;
    
    svg {
      width: 100%;
      height: 100%;
    }
  }
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  
  svg {
    width: 16px;
    height: 16px;
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
  }
  
  .user-name {
    font-weight: 500;
    color: #1e293b;
    
    .dark-mode & {
      color: #f1f5f9;
    }
  }
}

.content-wrapper {
  flex: 1;
  padding: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
