<template>
  <div class="admin-articles">
    <div class="toolbar">
      <el-button type="primary" @click="refresh">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>
    
    <el-table :data="articles" v-loading="loading" style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="category.name" label="分类" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="statistics.viewCount" label="浏览" width="80" />
      <el-table-column prop="statistics.likeCount" label="点赞" width="80" />
      <el-table-column prop="createdAt" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="viewArticle(row)">查看</el-button>
          <el-button text type="danger" @click="deleteArticle(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { articleApi } from '@/api'
import type { Article } from '@/types'

const router = useRouter()

const articles = ref<Article[]>([])
const loading = ref(false)
const currentPage = ref(1)
const total = ref(0)

onMounted(() => {
  loadArticles()
})

const loadArticles = async () => {
  loading.value = true
  try {
    const response = await articleApi.getArticles({ page: currentPage.value - 1, size: 10 })
    articles.value = response.content
    total.value = response.totalElements
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadArticles()
}

const viewArticle = (article: Article) => {
  router.push(`/article/${article.id}`)
}

const deleteArticle = async (article: Article) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', { type: 'warning' })
    await articleApi.deleteArticle(article.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch {}
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    ARCHIVED: 'warning',
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ARCHIVED: '已归档',
  }
  return texts[status] || status
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.admin-articles {
  .toolbar {
    margin-bottom: 16px;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
