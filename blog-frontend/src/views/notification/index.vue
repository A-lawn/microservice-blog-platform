<template>
  <div class="notifications-page">
    <div class="page-header">
      <h1>消息通知</h1>
      <el-button text @click="markAllRead" :disabled="notificationStore.unreadCount === 0">
        全部标记已读
      </el-button>
    </div>
    
    <div class="notification-list" v-loading="notificationStore.loading">
      <div
        v-for="notification in notificationStore.notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: !notification.isRead }"
        @click="handleClick(notification)"
      >
        <div class="notification-icon">
          <el-icon :size="24">
            <component :is="getIcon(notification.type)" />
          </el-icon>
        </div>
        <div class="notification-content">
          <h4>{{ notification.title }}</h4>
          <p>{{ notification.content }}</p>
          <span class="time">{{ formatTime(notification.createdAt) }}</span>
        </div>
      </div>
      
      <el-empty v-if="!notificationStore.loading && notificationStore.notifications.length === 0" description="暂无通知" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useNotificationStore } from '@/stores'
import type { Notification } from '@/types'

const router = useRouter()
const notificationStore = useNotificationStore()

onMounted(() => {
  notificationStore.fetchNotifications({ page: 0, size: 50 })
})

const handleClick = async (notification: Notification) => {
  if (!notification.isRead) {
    await notificationStore.markAsRead(notification.id)
  }
  
  if (notification.referenceId) {
    switch (notification.referenceType) {
      case 'ARTICLE':
        router.push(`/article/${notification.referenceId}`)
        break
      case 'COMMENT':
        router.push(`/article/${notification.referenceId}`)
        break
      case 'USER':
        router.push(`/user/${notification.referenceId}`)
        break
    }
  }
}

const markAllRead = () => {
  notificationStore.markAllAsRead()
}

const getIcon = (type: string) => {
  const icons: Record<string, string> = {
    LIKE: 'Star',
    COMMENT: 'ChatDotRound',
    FOLLOW: 'User',
    MENTION: 'At',
    SYSTEM: 'Bell',
  }
  return icons[type] || 'Bell'
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.notifications-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      font-size: 24px;
    }
  }
  
  .notification-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .notification-item {
      display: flex;
      gap: 16px;
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: background 0.2s;
      
      &:hover {
        background: #f5f7fa;
      }
      
      &:last-child {
        border-bottom: none;
      }
      
      &.unread {
        background: #ecf5ff;
      }
      
      .notification-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #f0f2f5;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #409eff;
      }
      
      .notification-content {
        flex: 1;
        
        h4 {
          font-size: 15px;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 14px;
          color: #666;
          margin-bottom: 4px;
        }
        
        .time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>
