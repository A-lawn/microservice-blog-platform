<template>
  <div class="admin-users">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索用户..." style="width: 200px;" />
      <el-button type="primary" @click="refresh">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>
    
    <el-table :data="users" v-loading="loading" style="width: 100%">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="统计" width="200">
        <template #default="{ row }">
          <span>文章: {{ row.statistics?.articleCount || 0 }}</span>
          <span style="margin-left: 10px;">粉丝: {{ row.statistics?.followerCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="viewUser(row)">查看</el-button>
          <el-button text :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadUsers"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import type { User } from '@/types'

const router = useRouter()

const users = ref<User[]>([])
const loading = ref(false)
const currentPage = ref(1)
const total = ref(0)
const searchKeyword = ref('')

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadUsers()
}

const viewUser = (user: User) => {
  router.push(`/user/${user.id}`)
}

const toggleStatus = async (user: User) => {
  const action = user.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { type: 'warning' })
    user.status = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
    ElMessage.success(`${action}成功`)
  } catch {}
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.admin-users {
  .toolbar {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
