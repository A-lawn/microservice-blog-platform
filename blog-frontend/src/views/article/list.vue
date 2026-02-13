<template>
  <div class="article-list-page">
    <div class="page-header">
      <h1>文章列表</h1>
      <el-button type="primary" @click="router.push('/write')">
        <el-icon><Plus /></el-icon> 写文章
      </el-button>
    </div>
    
    <div class="filter-bar">
      <el-select v-model="filters.categoryId" placeholder="选择分类" clearable @change="handleFilter">
        <el-option
          v-for="category in articleStore.categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
      
      <el-select v-model="filters.tag" placeholder="选择标签" clearable @change="handleFilter">
        <el-option
          v-for="tag in articleStore.tags"
          :key="tag.id"
          :label="tag.name"
          :value="tag.name"
        />
      </el-select>
      
      <el-select v-model="filters.sort" placeholder="排序方式" @change="handleFilter">
        <el-option label="最新发布" value="publishTime,desc" />
        <el-option label="最多浏览" value="viewCount,desc" />
        <el-option label="最多点赞" value="likeCount,desc" />
      </el-select>
    </div>
    
    <div class="article-list" v-loading="articleStore.loading">
      <article-card
        v-for="article in articleStore.articles"
        :key="article.id"
        :article="article"
      />
      
      <el-empty v-if="!articleStore.loading && articleStore.articles.length === 0" description="暂无文章" />
    </div>
    
    <div class="pagination" v-if="articleStore.total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="10"
        :total="articleStore.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores'
import ArticleCard from '@/components/ArticleCard.vue'

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const currentPage = ref(1)
const filters = reactive({
  categoryId: null as number | null,
  tag: null as string | null,
  sort: 'publishTime,desc',
})

const loadArticles = () => {
  articleStore.fetchArticles({
    page: currentPage.value - 1,
    size: 10,
    categoryId: filters.categoryId || undefined,
    tag: filters.tag || undefined,
    sort: filters.sort,
  })
}

onMounted(() => {
  articleStore.fetchCategories()
  articleStore.fetchTags()
  loadArticles()
})

watch(() => route.query, () => {
  if (route.query.categoryId) {
    filters.categoryId = Number(route.query.categoryId)
  }
  if (route.query.tag) {
    filters.tag = route.query.tag as string
  }
  loadArticles()
}, { immediate: true })

const handleFilter = () => {
  currentPage.value = 1
  loadArticles()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style lang="scss" scoped>
.article-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h1 {
      font-size: 24px;
      font-weight: 600;
    }
  }
  
  .filter-bar {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
    
    .el-select {
      width: 150px;
    }
  }
  
  .article-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 30px;
  }
}
</style>
