<template>
  <div class="search-page">
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="搜索文章..."
        size="large"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </template>
      </el-input>
    </div>
    
    <div class="search-results" v-loading="loading">
      <p class="result-count" v-if="total > 0">
        找到 {{ total }} 篇相关文章
      </p>
      
      <div class="article-list">
        <article-card
          v-for="article in articles"
          :key="article.id"
          :article="article"
        />
      </div>
      
      <el-empty v-if="!loading && articles.length === 0 && keyword" description="未找到相关文章" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api'
import type { Article } from '@/types'
import ArticleCard from '@/components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const articles = ref<Article[]>([])
const total = ref(0)
const loading = ref(false)

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
    handleSearch()
  }
})

watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    keyword.value = newKeyword as string
    handleSearch()
  }
})

const handleSearch = async () => {
  if (!keyword.value.trim()) return
  
  router.push({ path: '/search', query: { keyword: keyword.value } })
  
  loading.value = true
  try {
    const response = await articleApi.searchArticles(keyword.value, { page: 0, size: 20 })
    articles.value = response.content
    total.value = response.totalElements
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.search-page {
  .search-header {
    margin-bottom: 24px;
  }
  
  .result-count {
    color: #666;
    margin-bottom: 16px;
  }
  
  .article-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}
</style>
