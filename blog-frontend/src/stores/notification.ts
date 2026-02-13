import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api'
import type { Notification, PageParams, PageResponse } from '@/types'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const total = ref(0)

  async function fetchNotifications(params?: PageParams): Promise<PageResponse<Notification>> {
    loading.value = true
    try {
      const response = await notificationApi.getNotifications(params)
      notifications.value = response.content
      total.value = response.totalElements
      return response
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      const response = await notificationApi.getUnreadCount()
      unreadCount.value = response.count
    } catch (e) {
      console.error('Failed to fetch unread count:', e)
    }
  }

  async function markAsRead(id: number) {
    try {
      await notificationApi.markAsRead(id)
      const notification = notifications.value.find((n) => n.id === id)
      if (notification && !notification.isRead) {
        notification.isRead = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    } catch (e) {
      console.error('Failed to mark as read:', e)
    }
  }

  async function markAllAsRead() {
    try {
      await notificationApi.markAllAsRead()
      notifications.value.forEach((n) => (n.isRead = true))
      unreadCount.value = 0
    } catch (e) {
      console.error('Failed to mark all as read:', e)
    }
  }

  async function deleteNotification(id: number) {
    try {
      await notificationApi.deleteNotification(id)
      notifications.value = notifications.value.filter((n) => n.id !== id)
      total.value--
    } catch (e) {
      console.error('Failed to delete notification:', e)
    }
  }

  return {
    notifications,
    unreadCount,
    loading,
    total,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
  }
})
