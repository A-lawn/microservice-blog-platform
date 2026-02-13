<template>
  <div class="article-card hover-lift" @click="goToDetail">
    <div class="card-cover" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" />
      <div class="cover-overlay"></div>
    </div>
    <div class="card-content">
      <div class="card-header">
        <div class="author-info">
          <el-avatar :size="28" :src="article.authorAvatar" class="author-avatar">
            {{ article.authorName?.charAt(0) }}
          </el-avatar>
          <span class="author-name">{{ article.authorName }}</span>
        </div>
        <span class="publish-time">{{ formatTime(article.publishTime || article.createdAt) }}</span>
      </div>
      
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary }}</p>
      
      <div class="card-tags" v-if="article.tags?.length">
        <span v-for="tag in article.tags.slice(0, 3)" :key="tag" class="tag">
          {{ tag }}
        </span>
      </div>
      
      <div class="card-footer">
        <div class="stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ formatCount(article.statistics.viewCount) }}
          </span>
          <span class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            {{ formatCount(article.statistics.commentCount) }}
          </span>
          <span class="stat-item">
            <el-icon><Star /></el-icon>
            {{ formatCount(article.statistics.likeCount) }}
          </span>
        </div>
        <span class="read-more">
          阅读
          <el-icon><ArrowRight /></el-icon>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import type { Article } from '@/types'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  article: Article
}>()

const router = useRouter()

const goToDetail = () => {
  router.push(`/article/${props.article.id}`)
}

const formatTime = (time: string) => {
  const date = dayjs(time)
  const now = dayjs()
  const diff = now.diff(date, 'day')
  
  if (diff < 7) {
    return date.fromNow()
  }
  return date.format('MM-DD')
}

const formatCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}
</script>

<style lang="scss" scoped>
.article-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  
  &:hover {
    border-color: rgba(99, 102, 241, 0.3);
    box-shadow: var(--shadow-lg);
    
    .card-cover img {
      transform: scale(1.05);
    }
    
    .read-more {
      color: var(--primary-color);
      
      .el-icon {
        transform: translateX(4px);
      }
    }
  }
  
  .card-cover {
    position: relative;
    height: 180px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--transition-slow);
    }
    
    .cover-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.5));
    }
  }
  
  .card-content {
    padding: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .author-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .author-avatar {
        border: 2px solid var(--border-color);
      }
      
      .author-name {
        font-size: 13px;
        color: var(--text-secondary);
        font-weight: 500;
      }
    }
    
    .publish-time {
      font-size: 12px;
      color: var(--text-muted);
    }
  }
  
  .card-title {
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    margin-bottom: 8px;
    color: var(--text-primary);
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
  
  .card-summary {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.6;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
  
  .card-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    
    .tag {
      padding: 4px 10px;
      font-size: 12px;
      background: rgba(99, 102, 241, 0.1);
      color: var(--primary-color);
      border-radius: 9999px;
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid var(--border-color);
    
    .stats {
      display: flex;
      gap: 16px;
      
      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: var(--text-muted);
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
    
    .read-more {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--text-secondary);
      transition: color var(--transition-fast);
      
      .el-icon {
        transition: transform var(--transition-fast);
      }
    }
  }
}
</style>
