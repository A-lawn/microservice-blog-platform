import request from '@/utils/request'
import type { Comment, CreateCommentRequest, PageParams, PageResponse } from '@/types'

export const commentApi = {
  getCommentsByArticle(articleId: string, params?: PageParams): Promise<PageResponse<Comment>> {
    return request.get(`/comments/article/${articleId}`, { params })
  },

  getCommentReplies(commentId: string, params?: PageParams): Promise<PageResponse<Comment>> {
    return request.get(`/comments/${commentId}/replies`, { params })
  },

  createComment(data: CreateCommentRequest): Promise<Comment> {
    return request.post('/comments', data)
  },

  deleteComment(id: string): Promise<void> {
    return request.delete(`/comments/${id}`)
  },

  likeComment(id: string): Promise<void> {
    return request.post(`/comments/${id}/like`)
  },

  unlikeComment(id: string): Promise<void> {
    return request.delete(`/comments/${id}/like`)
  },

  reportComment(id: string, reason: string, description?: string): Promise<void> {
    return request.post(`/comments/${id}/report`, { reason, description })
  },

  getMyComments(params?: PageParams): Promise<PageResponse<Comment>> {
    return request.get('/comments/my', { params })
  },
}
