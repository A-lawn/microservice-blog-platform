import request from '@/utils/request'
import type { Article, CreateArticleRequest, PageParams, PageResponse, Category, Tag } from '@/types'

export const articleApi = {
  getArticles(params?: PageParams & { status?: string; categoryId?: number; tag?: string }): Promise<PageResponse<Article>> {
    return request.get('/articles', { params })
  },

  getArticleById(id: string): Promise<Article> {
    return request.get(`/articles/${id}`)
  },

  getArticleBySlug(slug: string): Promise<Article> {
    return request.get(`/articles/slug/${slug}`)
  },

  createArticle(data: CreateArticleRequest): Promise<Article> {
    return request.post('/articles', data)
  },

  updateArticle(id: string, data: Partial<CreateArticleRequest>): Promise<Article> {
    return request.put(`/articles/${id}`, data)
  },

  deleteArticle(id: string): Promise<void> {
    return request.delete(`/articles/${id}`)
  },

  publishArticle(id: string): Promise<void> {
    return request.put(`/articles/${id}/publish`)
  },

  archiveArticle(id: string): Promise<void> {
    return request.post(`/articles/${id}/archive`)
  },

  likeArticle(id: string): Promise<void> {
    return request.post(`/articles/${id}/like`)
  },

  unlikeArticle(id: string): Promise<void> {
    return request.delete(`/articles/${id}/like`)
  },

  bookmarkArticle(id: string): Promise<void> {
    return request.post(`/articles/${id}/bookmark`)
  },

  unbookmarkArticle(id: string): Promise<void> {
    return request.delete(`/articles/${id}/bookmark`)
  },

  getLikeStatus(id: string): Promise<{ liked: boolean; bookmarked: boolean }> {
    return request.get(`/articles/${id}/like-status`)
  },

  getMyArticles(params?: PageParams): Promise<PageResponse<Article>> {
    return request.get('/articles/my', { params })
  },

  getBookmarkedArticles(params?: PageParams): Promise<PageResponse<Article>> {
    return request.get('/articles/bookmarked', { params })
  },

  searchArticles(keyword: string, params?: PageParams): Promise<PageResponse<Article>> {
    return request.get('/articles/search', { params: { keyword, ...params } })
  },

  getCategories(): Promise<Category[]> {
    return request.get('/categories')
  },

  createCategory(data: Partial<Category>): Promise<Category> {
    return request.post('/categories', data)
  },

  updateCategory(id: number, data: Partial<Category>): Promise<Category> {
    return request.put(`/categories/${id}`, data)
  },

  deleteCategory(id: number): Promise<void> {
    return request.delete(`/categories/${id}`)
  },

  getTags(): Promise<Tag[]> {
    return request.get('/tags')
  },

  getPopularTags(limit = 20): Promise<Tag[]> {
    return request.get('/tags/popular', { params: { limit } })
  },

  createTag(data: Partial<Tag>): Promise<Tag> {
    return request.post('/tags', data)
  },
}
