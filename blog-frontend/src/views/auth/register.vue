<template>
  <div class="register-page">
    <div class="register-card">
      <h2>用户注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      email: form.email,
      password: form.password,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e: any) {
    const message = e?.response?.data?.message || e?.message || '注册失败'
    ElMessage.error(message)
    console.error('Register failed:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .register-card {
    width: 420px;
    background: #fff;
    border-radius: 16px;
    padding: 40px 45px;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.25);
    
    h2 {
      text-align: center;
      margin-bottom: 35px;
      color: #333;
      font-size: 26px;
      font-weight: 600;
    }
    
    :deep(.el-form-item) {
      margin-bottom: 24px;
    }
    
    :deep(.el-input) {
      --el-input-border-radius: 8px;
      
      .el-input__wrapper {
        padding: 4px 15px;
        background: #fff !important;
        border: 2px solid #e0e0e0 !important;
        box-shadow: none !important;
        transition: all 0.3s;
        
        &:hover {
          border-color: #409eff !important;
        }
        
        &.is-focus {
          border-color: #409eff !important;
          box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2) !important;
        }
      }
      
      .el-input__inner {
        height: 44px;
        line-height: 44px;
        font-size: 15px;
        color: #333 !important;
        
        &::placeholder {
          color: #999 !important;
          font-size: 14px;
        }
      }
      
      .el-input__prefix {
        font-size: 18px;
        color: #666;
      }
      
      .el-input__suffix {
        .el-input__icon {
          color: #666;
        }
      }
    }
    
    :deep(.el-button) {
      height: 48px;
      font-size: 16px;
      border-radius: 8px;
      font-weight: 500;
      letter-spacing: 2px;
      
      &.el-button--primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
        border: none !important;
        
        &:hover {
          opacity: 0.9;
        }
      }
    }
    
    .footer {
      text-align: center;
      margin-top: 25px;
      color: #666;
      font-size: 14px;
      
      a {
        color: #409eff;
        margin-left: 4px;
        font-weight: 500;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}
</style>
