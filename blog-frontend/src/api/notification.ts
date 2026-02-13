import request from '@/utils/request'
import type { Notification, PageParams, PageResponse } from '@/types'

export const notificationApi = {
  getNotifications(params?: PageParams): Promise<PageResponse<Notification>> {
    return request.get('/notifications', { params })
  },

  getUnreadCount(): Promise<{ count: number }> {
    return request.get('/notifications/unread-count')
  },

  markAsRead(id: number): Promise<void> {
    return request.put(`/notifications/${id}/read`)
  },

  markAllAsRead(): Promise<void> {
    return request.put('/notifications/read-all')
  },

  deleteNotification(id: number): Promise<void> {
    return request.delete(`/notifications/${id}`)
  },
}
