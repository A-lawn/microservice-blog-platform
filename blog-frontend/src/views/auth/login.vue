<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>
    
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <span class="logo-text gradient-text">BlogX</span>
        </div>
        <h1 class="welcome-title">
          欢迎回来
          <span class="gradient-text">探索者</span>
        </h1>
        <p class="welcome-desc">
          登录你的账户，继续你的创作之旅
        </p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Edit /></el-icon>
            <span>创作精彩内容</span>
          </div>
          <div class="feature-item">
            <el-icon><User /></el-icon>
            <span>建立个人品牌</span>
          </div>
          <div class="feature-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>与读者互动</span>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-card glass">
          <h2 class="card-title">登录账户</h2>
          
          <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="用户名"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                size="large"
                show-password
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>
            
            <el-form-item>
              <button type="button" class="btn btn-primary btn-block" :loading="loading" @click="handleLogin">
                登录
              </button>
            </el-form-item>
          </el-form>
          
          <div class="divider">
            <span>或</span>
          </div>
          
          <div class="social-login">
            <button class="social-btn">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.604-3.369-1.341-3.369-1.341-.454-1.155-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.163 22 16.418 22 12c0-5.523-4.477-10-10-10z"/>
              </svg>
              GitHub
            </button>
            <button class="social-btn">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M22.46 6c-.77.35-1.6.58-2.46.69.88-.53 1.56-1.37 1.88-2.38-.83.5-1.75.85-2.72 1.05C18.37 4.5 17.26 4 16 4c-2.35 0-4.27 1.92-4.27 4.29 0 .34.04.67.11.98C8.28 9.09 5.11 7.38 3 4.79c-.37.63-.58 1.37-.58 2.15 0 1.49.75 2.81 1.91 3.56-.71 0-1.37-.2-1.95-.5v.03c0 2.08 1.48 3.82 3.44 4.21a4.22 4.22 0 0 1-1.93.07 4.28 4.28 0 0 0 4 2.98 8.521 8.521 0 0 1-5.33 1.84c-.34 0-.68-.02-1.02-.06C3.44 20.29 5.7 21 8.12 21 16 21 20.33 14.46 20.33 8.79c0-.19 0-.37-.01-.56.84-.6 1.56-1.36 2.14-2.23z"/>
              </svg>
              Twitter
            </button>
          </div>
          
          <div class="register-link">
            还没有账户？ <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (e) {
    console.error('Login failed:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  .login-bg {
    position: absolute;
    inset: 0;
    
    .gradient-orb {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      
      &.orb-1 {
        width: 600px;
        height: 600px;
        background: rgba(99, 102, 241, 0.3);
        top: -200px;
        left: -200px;
        animation: float 20s infinite ease-in-out;
      }
      
      &.orb-2 {
        width: 500px;
        height: 500px;
        background: rgba(236, 72, 153, 0.2);
        bottom: -150px;
        right: -150px;
        animation: float 25s infinite ease-in-out reverse;
      }
      
      &.orb-3 {
        width: 400px;
        height: 400px;
        background: rgba(6, 182, 212, 0.2);
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        animation: pulse 15s infinite ease-in-out;
      }
    }
  }
  
  .login-container {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 80px;
    max-width: 1000px;
    width: 100%;
    padding: 0 40px;
  }
  
  .login-left {
    max-width: 400px;
    
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 32px;
      
      .logo-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--gradient-primary);
        border-radius: var(--radius-md);
        color: white;
        
        svg {
          width: 28px;
          height: 28px;
        }
      }
      
      .logo-text {
        font-size: 28px;
        font-weight: 700;
      }
    }
    
    .welcome-title {
      font-size: 48px;
      font-weight: 800;
      line-height: 1.2;
      margin-bottom: 16px;
      
      span {
        display: block;
      }
    }
    
    .welcome-desc {
      font-size: 18px;
      color: var(--text-secondary);
      margin-bottom: 40px;
    }
    
    .features {
      display: flex;
      flex-direction: column;
      gap: 16px;
      
      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        color: var(--text-secondary);
        
        .el-icon {
          font-size: 20px;
          color: var(--primary-color);
        }
      }
    }
  }
  
  .login-right {
    flex: 1;
    max-width: 420px;
    
    .login-card {
      padding: 40px;
      border-radius: var(--radius-xl);
      
      .card-title {
        font-size: 24px;
        font-weight: 600;
        margin-bottom: 32px;
        text-align: center;
      }
      
      .login-form {
        .custom-input {
          :deep(.el-input__wrapper) {
            background: var(--bg-glass) !important;
            border: 1px solid var(--border-color) !important;
            border-radius: var(--radius-md) !important;
            padding: 4px 12px;
          }
        }
        
        .form-options {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 24px;
          
          .forgot-link {
            font-size: 13px;
            color: var(--primary-color);
          }
        }
        
        .btn-block {
          width: 100%;
          padding: 14px;
          font-size: 16px;
        }
      }
      
      .divider {
        display: flex;
        align-items: center;
        margin: 24px 0;
        
        &::before,
        &::after {
          content: '';
          flex: 1;
          height: 1px;
          background: var(--border-color);
        }
        
        span {
          padding: 0 16px;
          color: var(--text-muted);
          font-size: 13px;
        }
      }
      
      .social-login {
        display: flex;
        gap: 12px;
        
        .social-btn {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          padding: 12px;
          background: var(--bg-glass);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-md);
          color: var(--text-secondary);
          font-size: 14px;
          cursor: pointer;
          transition: all var(--transition-fast);
          
          &:hover {
            border-color: var(--primary-color);
            color: var(--primary-color);
          }
        }
      }
      
      .register-link {
        text-align: center;
        margin-top: 24px;
        color: var(--text-secondary);
        font-size: 14px;
        
        a {
          color: var(--primary-color);
          font-weight: 500;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .login-page {
    .login-container {
      flex-direction: column;
      gap: 40px;
    }
    
    .login-left {
      text-align: center;
      
      .welcome-title {
        font-size: 32px;
      }
      
      .features {
        align-items: center;
      }
    }
  }
}
</style>
