<template>
  <div class="user-profile-page" v-loading="loading">
    <div class="user-header" v-if="user">
      <div class="user-info">
        <el-avatar :size="80" :src="user.avatarUrl">
          {{ user.nickname?.charAt(0) }}
        </el-avatar>
        <div class="user-detail">
          <h2>{{ user.nickname || user.username }}</h2>
          <p class="bio">{{ user.bio || '这个人很懒，什么都没写...' }}</p>
          <p class="joined">加入于 {{ formatTime(user.createdAt) }}</p>
        </div>
      </div>
      <div class="user-actions" v-if="userStore.isLoggedIn && userStore.user?.id !== user.id">
        <el-button :type="isFollowing ? 'default' : 'primary'" @click="toggleFollow">
          {{ isFollowing ? '已关注' : '关注' }}
        </el-button>
      </div>
    </div>
    
    <div class="stats-bar" v-if="user">
      <div class="stat-item">
        <span class="value">{{ user.statistics.articleCount }}</span>
        <span class="label">文章</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ user.statistics.followerCount }}</span>
        <span class="label">粉丝</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ user.statistics.followingCount }}</span>
        <span class="label">关注</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ user.statistics.likeCount }}</span>
        <span class="label">获赞</span>
      </div>
    </div>
    
    <div class="user-articles">
      <h3>TA的文章</h3>
      <div class="article-list" v-loading="articlesLoading">
        <article-card
          v-for="article in articles"
          :key="article.id"
          :article="article"
        />
        <el-empty v-if="!articlesLoading && articles.length === 0" description="暂无文章" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores'
import { userApi, articleApi } from '@/api'
import type { User, Article } from '@/types'
import ArticleCard from '@/components/ArticleCard.vue'

const route = useRoute()
const userStore = useUserStore()

const user = ref<User | null>(null)
const loading = ref(true)
const articles = ref<Article[]>([])
const articlesLoading = ref(false)
const isFollowing = ref(false)

onMounted(async () => {
  await loadUser()
  await loadArticles()
})

const loadUser = async () => {
  loading.value = true
  try {
    user.value = await userApi.getUserById(route.params.id as string)
  } finally {
    loading.value = false
  }
}

const loadArticles = async () => {
  articlesLoading.value = true
  try {
    const response = await articleApi.getArticles({
      page: 0,
      size: 10,
    })
    articles.value = response.content.filter(a => a.authorId === route.params.id)
  } finally {
    articlesLoading.value = false
  }
}

const toggleFollow = async () => {
  if (!user.value) return
  if (isFollowing.value) {
    await userApi.unfollowUser(user.value.id)
    isFollowing.value = false
    user.value.statistics.followerCount--
  } else {
    await userApi.followUser(user.value.id)
    isFollowing.value = true
    user.value.statistics.followerCount++
  }
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD')
}
</script>

<style lang="scss" scoped>
.user-profile-page {
  .user-header {
    background: #fff;
    border-radius: 8px;
    padding: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .user-detail {
        h2 {
          font-size: 24px;
          margin-bottom: 8px;
        }
        
        .bio {
          color: #666;
          margin-bottom: 8px;
        }
        
        .joined {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }
  
  .stats-bar {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    justify-content: space-around;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .stat-item {
      text-align: center;
      
      .value {
        display: block;
        font-size: 24px;
        font-weight: 600;
        color: #333;
      }
      
      .label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .user-articles {
    h3 {
      margin-bottom: 16px;
    }
    
    .article-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
  }
}
</style>
