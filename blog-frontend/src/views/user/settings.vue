<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>个人设置</h1>
    </div>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="profile">
        <el-form :model="profileForm" label-width="80px" class="settings-form">
          <el-form-item label="头像">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <el-avatar :size="80" :src="userStore.user?.avatarUrl">
                {{ userStore.user?.nickname?.charAt(0) }}
              </el-avatar>
              <div class="upload-text">点击上传</div>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          
          <el-form-item label="个人简介">
            <el-input
              v-model="profileForm.bio"
              type="textarea"
              :rows="4"
              placeholder="介绍一下自己..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="settings-form">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="changePassword" :loading="changingPassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { fileApi, userApi } from '@/api'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'

const userStore = useUserStore()

const activeTab = ref('profile')
const saving = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref<FormInstance>()

const profileForm = reactive({
  nickname: '',
  bio: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

onMounted(() => {
  if (userStore.user) {
    profileForm.nickname = userStore.user.nickname || ''
    profileForm.bio = userStore.user.bio || ''
  }
})

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const uploadAvatar = async (options: UploadRequestOptions) => {
  try {
    const response = await fileApi.uploadFile(options.file as File)
    await userApi.updateAvatar(response.url)
    userStore.user!.avatarUrl = response.url
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    await userStore.updateProfile(profileForm)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  const valid = await passwordFormRef.value?.validate()
  if (!valid) return
  
  changingPassword.value = true
  try {
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } finally {
    changingPassword.value = false
  }
}
</script>

<style lang="scss" scoped>
.settings-page {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      font-size: 24px;
    }
  }
  
  .settings-form {
    max-width: 500px;
    background: #fff;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .avatar-uploader {
    position: relative;
    cursor: pointer;
    
    .upload-text {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      text-align: center;
      font-size: 12px;
      padding: 4px 0;
      border-radius: 0 0 40px 40px;
    }
  }
}
</style>
