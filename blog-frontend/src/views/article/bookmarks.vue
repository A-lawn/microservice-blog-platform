<template>
  <div class="bookmarks-page">
    <div class="page-header">
      <h1>我的收藏</h1>
    </div>
    
    <div class="article-list" v-loading="loading">
      <article-card
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />
      
      <el-empty v-if="!loading && articles.length === 0" description="暂无收藏" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api'
import type { Article } from '@/types'
import ArticleCard from '@/components/ArticleCard.vue'

const articles = ref<Article[]>([])
const loading = ref(false)

onMounted(() => {
  loadBookmarks()
})

const loadBookmarks = async () => {
  loading.value = true
  try {
    const response = await articleApi.getBookmarkedArticles({ page: 0, size: 20 })
    articles.value = response.content
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.bookmarks-page {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      font-size: 24px;
    }
  }
  
  .article-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}
</style>
