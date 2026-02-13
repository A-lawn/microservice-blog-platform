import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { articleApi } from '@/api'
import type { Article, Category, Tag, PageParams, CreateArticleRequest } from '@/types'

export const useArticleStore = defineStore('article', () => {
  const articles = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const categories = ref<Category[]>([])
  const tags = ref<Tag[]>([])
  const popularTags = ref<Tag[]>([])
  const loading = ref(false)
  const total = ref(0)
  const hasMore = ref(true)

  const articleCount = computed(() => total.value)

  async function fetchArticles(params?: PageParams & { status?: string; categoryId?: number; tag?: string }) {
    loading.value = true
    try {
      const response = await articleApi.getArticles(params)
      if (params?.page === 0 || !params?.page) {
        articles.value = response.content
      } else {
        articles.value.push(...response.content)
      }
      total.value = response.totalElements
      hasMore.value = !response.last
      return response
    } finally {
      loading.value = false
    }
  }

  async function fetchArticleById(id: string) {
    loading.value = true
    try {
      currentArticle.value = await articleApi.getArticleById(id)
      return currentArticle.value
    } finally {
      loading.value = false
    }
  }

  async function fetchArticleBySlug(slug: string) {
    loading.value = true
    try {
      currentArticle.value = await articleApi.getArticleBySlug(slug)
      return currentArticle.value
    } finally {
      loading.value = false
    }
  }

  async function createArticle(data: CreateArticleRequest) {
    loading.value = true
    try {
      const article = await articleApi.createArticle(data)
      articles.value.unshift(article)
      total.value++
      return article
    } finally {
      loading.value = false
    }
  }

  async function updateArticle(id: string, data: Partial<CreateArticleRequest>) {
    loading.value = true
    try {
      const article = await articleApi.updateArticle(id, data)
      const index = articles.value.findIndex((a) => a.id === id)
      if (index !== -1) {
        articles.value[index] = article
      }
      if (currentArticle.value?.id === id) {
        currentArticle.value = article
      }
      return article
    } finally {
      loading.value = false
    }
  }

  async function deleteArticle(id: string) {
    loading.value = true
    try {
      await articleApi.deleteArticle(id)
      articles.value = articles.value.filter((a) => a.id !== id)
      total.value--
      if (currentArticle.value?.id === id) {
        currentArticle.value = null
      }
    } finally {
      loading.value = false
    }
  }

  async function publishArticle(id: string) {
    loading.value = true
    try {
      await articleApi.publishArticle(id)
      const article = articles.value.find((a) => a.id === id)
      if (article) {
        article.status = 'PUBLISHED'
      }
      if (currentArticle.value?.id === id) {
        currentArticle.value.status = 'PUBLISHED'
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchCategories() {
    try {
      categories.value = await articleApi.getCategories()
    } catch (e) {
      console.error('Failed to fetch categories:', e)
    }
  }

  async function fetchTags() {
    try {
      tags.value = await articleApi.getTags()
    } catch (e) {
      console.error('Failed to fetch tags:', e)
    }
  }

  async function fetchPopularTags(limit = 20) {
    try {
      popularTags.value = await articleApi.getPopularTags(limit)
    } catch (e) {
      console.error('Failed to fetch popular tags:', e)
    }
  }

  async function searchArticles(keyword: string, params?: PageParams) {
    loading.value = true
    try {
      const response = await articleApi.searchArticles(keyword, params)
      articles.value = response.content
      total.value = response.totalElements
      hasMore.value = !response.last
      return response
    } finally {
      loading.value = false
    }
  }

  function clearArticles() {
    articles.value = []
    currentArticle.value = null
    total.value = 0
    hasMore.value = true
  }

  return {
    articles,
    currentArticle,
    categories,
    tags,
    popularTags,
    loading,
    total,
    hasMore,
    articleCount,
    fetchArticles,
    fetchArticleById,
    fetchArticleBySlug,
    createArticle,
    updateArticle,
    deleteArticle,
    publishArticle,
    fetchCategories,
    fetchTags,
    fetchPopularTags,
    searchArticles,
    clearArticles,
  }
})
