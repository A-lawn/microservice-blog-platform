<template>
  <div class="article-detail-page" v-loading="loading">
    <article class="article" v-if="article">
      <header class="article-header">
        <h1 class="title">{{ article.title }}</h1>
        <div class="meta">
          <div class="author-info">
            <el-avatar :size="40" :src="article.authorAvatar">
              {{ article.authorName?.charAt(0) }}
            </el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ article.authorName }}</span>
              <span class="publish-time">{{ formatTime(article.publishTime) }}</span>
            </div>
          </div>
          <div class="stats">
            <span><el-icon><View /></el-icon> {{ article.statistics.viewCount }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ article.statistics.commentCount }}</span>
            <span><el-icon><Star /></el-icon> {{ article.statistics.likeCount }}</span>
          </div>
        </div>
        <div class="tags" v-if="article.tags?.length">
          <el-tag v-for="tag in article.tags" :key="tag" size="small">{{ tag }}</el-tag>
        </div>
      </header>
      
      <div class="article-cover" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />
      </div>
      
      <div class="article-content" v-html="renderedContent"></div>
      
      <div class="article-actions">
        <el-button :type="liked ? 'primary' : 'default'" @click="handleLike">
          <el-icon><Star /></el-icon> {{ liked ? '已点赞' : '点赞' }}
        </el-button>
        <el-button :type="bookmarked ? 'warning' : 'default'" @click="handleBookmark">
          <el-icon><CollectionTag /></el-icon> {{ bookmarked ? '已收藏' : '收藏' }}
        </el-button>
        <el-button @click="handleShare">
          <el-icon><Share /></el-icon> 分享
        </el-button>
      </div>
    </article>
    
    <div class="comment-section" v-if="article">
      <h3>评论 ({{ article.statistics.commentCount }})</h3>
      <div class="comment-input">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
        />
        <el-button type="primary" @click="submitComment" :loading="submitting">
          发表评论
        </el-button>
      </div>
      
      <div class="comment-list">
        <div class="comment-item" v-for="comment in comments" :key="comment.id">
          <div class="comment-avatar">
            <el-avatar :size="36" :src="comment.authorAvatar">
              {{ comment.authorName?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="comment-content">
            <div class="comment-header">
              <span class="author-name">{{ comment.authorName }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
            <div class="comment-actions">
              <el-button text size="small" @click="likeComment(comment.id)">
                <el-icon><Star /></el-icon> {{ comment.likeCount }}
              </el-button>
              <el-button text size="small" @click="replyTo(comment)">
                回复
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import dayjs from 'dayjs'
import { useArticleStore, useUserStore } from '@/stores'
import { articleApi, commentApi } from '@/api'
import type { Article, Comment } from '@/types'

const route = useRoute()
const articleStore = useArticleStore()
const userStore = useUserStore()

const article = ref<Article | null>(null)
const loading = ref(true)
const liked = ref(false)
const bookmarked = ref(false)
const commentContent = ref('')
const comments = ref<Comment[]>([])
const submitting = ref(false)

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  const html = marked(article.value.content)
  return DOMPurify.sanitize(html)
})

onMounted(async () => {
  const id = route.params.id as string
  await loadArticle(id)
  await loadComments(id)
})

const loadArticle = async (id: string) => {
  loading.value = true
  try {
    article.value = await articleStore.fetchArticleById(id)
    if (userStore.isLoggedIn) {
      const status = await articleApi.getLikeStatus(id)
      liked.value = status.liked
      bookmarked.value = status.bookmarked
    }
  } finally {
    loading.value = false
  }
}

const loadComments = async (articleId: string) => {
  const response = await commentApi.getCommentsByArticle(articleId, { page: 0, size: 20 })
  comments.value = response.content
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (liked.value) {
    await articleApi.unlikeArticle(article.value!.id)
    liked.value = false
    article.value!.statistics.likeCount--
  } else {
    await articleApi.likeArticle(article.value!.id)
    liked.value = true
    article.value!.statistics.likeCount++
  }
}

const handleBookmark = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (bookmarked.value) {
    await articleApi.unbookmarkArticle(article.value!.id)
    bookmarked.value = false
    ElMessage.success('已取消收藏')
  } else {
    await articleApi.bookmarkArticle(article.value!.id)
    bookmarked.value = true
    ElMessage.success('收藏成功')
  }
}

const handleShare = () => {
  navigator.clipboard.writeText(window.location.href)
  ElMessage.success('链接已复制到剪贴板')
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    const comment = await commentApi.createComment({
      articleId: article.value!.id,
      content: commentContent.value,
    })
    comments.value.unshift(comment)
    commentContent.value = ''
    ElMessage.success('评论发表成功')
  } finally {
    submitting.value = false
  }
}

const likeComment = async (commentId: string) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  await commentApi.likeComment(commentId)
}

const replyTo = (comment: Comment) => {
  commentContent.value = `@${comment.authorName} `
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.article-detail-page {
  max-width: 800px;
  margin: 0 auto;
  
  .article {
    background: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .article-header {
      margin-bottom: 24px;
      
      .title {
        font-size: 28px;
        font-weight: 700;
        line-height: 1.4;
        margin-bottom: 16px;
        color: #1a1a1a;
        word-break: break-word;
      }
      
      .meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        .author-info {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .author-detail {
            display: flex;
            flex-direction: column;
            
            .author-name {
              font-weight: 500;
              color: #333;
            }
            
            .publish-time {
              font-size: 13px;
              color: #909399;
            }
          }
        }
        
        .stats {
          display: flex;
          gap: 16px;
          color: #909399;
          font-size: 14px;
          
          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
      
      .tags {
        display: flex;
        gap: 8px;
      }
    }
    
    .article-cover {
      margin-bottom: 24px;
      border-radius: 8px;
      overflow: hidden;
      
      img {
        width: 100%;
        display: block;
      }
    }
    
    .article-content {
      font-size: 16px;
      line-height: 1.8;
      color: #333;
      
      :deep(h1), :deep(h2), :deep(h3) {
        margin: 24px 0 16px;
        color: #1a1a1a;
        font-weight: 600;
      }
      
      :deep(p) {
        margin-bottom: 16px;
      }
      
      :deep(pre) {
        background: #f5f7fa;
        padding: 16px;
        border-radius: 6px;
        overflow-x: auto;
      }
      
      :deep(img) {
        max-width: 100%;
        border-radius: 4px;
      }
    }
    
    .article-actions {
      display: flex;
      gap: 12px;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid #eee;
    }
  }
  
  .comment-section {
    margin-top: 30px;
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    
    h3 {
      margin-bottom: 20px;
    }
    
    .comment-input {
      margin-bottom: 24px;
      
      .el-button {
        margin-top: 12px;
      }
    }
    
    .comment-list {
      .comment-item {
        display: flex;
        gap: 12px;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .comment-content {
          flex: 1;
          
          .comment-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
            
            .author-name {
              font-weight: 500;
            }
            
            .comment-time {
              font-size: 13px;
              color: #909399;
            }
          }
          
          .comment-text {
            color: #333;
            line-height: 1.6;
          }
          
          .comment-actions {
            margin-top: 8px;
          }
        }
      }
    }
  }
}
</style>
