<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-gradient"></div>
        <div class="hero-particles">
          <span v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></span>
        </div>
      </div>
      <div class="hero-content container">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="title-line">探索</span>
            <span class="title-line gradient-text">无限可能</span>
          </h1>
          <p class="hero-desc">
            在这里记录你的思考，分享你的知识，与志同道合的人一起成长
          </p>
          <div class="hero-actions">
            <button class="btn btn-primary btn-lg" @click="router.push('/write')">
              <el-icon><Edit /></el-icon>
              开始写作
            </button>
            <button class="btn btn-secondary btn-lg" @click="router.push('/articles')">
              <el-icon><Reading /></el-icon>
              探索文章
            </button>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-value">{{ formatNumber(stats.articleCount) }}</span>
              <span class="stat-label">篇文章</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ formatNumber(stats.userCount) }}</span>
              <span class="stat-label">位作者</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ formatNumber(stats.viewCount) }}</span>
              <span class="stat-label">次阅读</span>
            </div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="floating-cards">
            <div class="float-card card-1">
              <el-icon><Document /></el-icon>
              <span>Markdown</span>
            </div>
            <div class="float-card card-2">
              <el-icon><ChatDotRound /></el-icon>
              <span>评论互动</span>
            </div>
            <div class="float-card card-3">
              <el-icon><Search /></el-icon>
              <span>全文搜索</span>
            </div>
            <div class="float-card card-4">
              <el-icon><Star /></el-icon>
              <span>收藏分享</span>
            </div>
          </div>
        </div>
      </div>
      <div class="scroll-indicator">
        <el-icon><ArrowDown /></el-icon>
      </div>
    </section>
    
    <section class="content-section container">
      <div class="section-header">
        <div class="section-title">
          <h2>最新文章</h2>
          <p>发现精彩内容，获取最新资讯</p>
        </div>
        <button class="btn btn-secondary" @click="router.push('/articles')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
      
      <div class="articles-grid" v-loading="articleStore.loading">
        <article-card
          v-for="(article, index) in articleStore.articles.slice(0, 6)"
          :key="article.id"
          :article="article"
          :style="{ animationDelay: `${index * 0.1}s` }"
          class="article-item slide-up"
        />
      </div>
    </section>
    
    <section class="features-section container">
      <div class="section-header center">
        <h2>平台特色</h2>
        <p>为你提供最佳的写作与阅读体验</p>
      </div>
      
      <div class="features-grid">
        <div class="feature-card hover-lift">
          <div class="feature-icon">
            <el-icon><Edit /></el-icon>
          </div>
          <h3>Markdown 编辑器</h3>
          <p>支持实时预览的 Markdown 编辑器，让写作更加高效</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon">
            <el-icon><Search /></el-icon>
          </div>
          <h3>全文搜索</h3>
          <p>基于 Elasticsearch 的全文搜索，快速找到你需要的内容</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <h3>互动交流</h3>
          <p>评论、点赞、收藏，与作者和其他读者互动交流</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon">
            <el-icon><User /></el-icon>
          </div>
          <h3>个人主页</h3>
          <p>展示你的文章和成就，建立你的个人品牌</p>
        </div>
      </div>
    </section>
    
    <section class="sidebar-section container">
      <div class="content-grid">
        <div class="main-column">
          <div class="section-header">
            <h2>热门标签</h2>
          </div>
          <div class="tags-cloud">
            <span
              v-for="tag in articleStore.popularTags"
              :key="tag.id"
              class="tag-item"
              :style="{ fontSize: getTagSize(tag.articleCount) }"
              @click="filterByTag(tag.name)"
            >
              #{{ tag.name }}
            </span>
          </div>
        </div>
        
        <div class="side-column">
          <div class="sidebar-card">
            <h3>文章分类</h3>
            <div class="category-list">
              <div
                v-for="category in articleStore.categories"
                :key="category.id"
                class="category-item"
                @click="filterByCategory(category.id)"
              >
                <span class="category-name">{{ category.name }}</span>
                <span class="category-count">{{ category.articleCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useArticleStore } from '@/stores'
import ArticleCard from '@/components/ArticleCard.vue'

const router = useRouter()
const articleStore = useArticleStore()

const stats = reactive({
  articleCount: 1256,
  userCount: 3420,
  viewCount: 89600,
})

onMounted(() => {
  articleStore.fetchArticles({ page: 0, size: 6 })
  articleStore.fetchPopularTags(15)
  articleStore.fetchCategories()
})

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

const getParticleStyle = (index: number) => {
  const size = Math.random() * 4 + 2
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 5}s`,
    animationDuration: `${Math.random() * 10 + 10}s`,
  }
}

const getTagSize = (count: number) => {
  const min = 12
  const max = 20
  const size = Math.min(max, Math.max(min, min + count * 0.5))
  return `${size}px`
}

const filterByTag = (tag: string) => {
  router.push({ path: '/articles', query: { tag } })
}

const filterByCategory = (categoryId: number) => {
  router.push({ path: '/articles', query: { categoryId: String(categoryId) } })
}
</script>

<style lang="scss" scoped>
.home-page {
  overflow: hidden;
}

.hero-section {
  position: relative;
  min-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  
  .hero-bg {
    position: absolute;
    inset: 0;
    overflow: hidden;
    
    .hero-gradient {
      position: absolute;
      inset: 0;
      background: 
        radial-gradient(ellipse at 30% 20%, rgba(99, 102, 241, 0.3) 0%, transparent 50%),
        radial-gradient(ellipse at 70% 80%, rgba(236, 72, 153, 0.2) 0%, transparent 50%),
        radial-gradient(ellipse at 50% 50%, rgba(6, 182, 212, 0.1) 0%, transparent 70%);
    }
    
    .hero-particles {
      position: absolute;
      inset: 0;
      
      .particle {
        position: absolute;
        background: var(--primary-color);
        border-radius: 50%;
        opacity: 0.3;
        animation: float 15s infinite ease-in-out;
      }
    }
  }
  
  .hero-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 60px;
  }
  
  .hero-text {
    max-width: 600px;
    
    .hero-title {
      font-size: 64px;
      font-weight: 800;
      line-height: 1.1;
      margin-bottom: 24px;
      
      .title-line {
        display: block;
      }
    }
    
    .hero-desc {
      font-size: 18px;
      color: var(--text-secondary);
      line-height: 1.8;
      margin-bottom: 32px;
    }
    
    .hero-actions {
      display: flex;
      gap: 16px;
      margin-bottom: 48px;
      
      .btn-lg {
        padding: 16px 32px;
        font-size: 16px;
      }
    }
    
    .hero-stats {
      display: flex;
      gap: 48px;
      
      .stat-item {
        .stat-value {
          display: block;
          font-size: 32px;
          font-weight: 700;
          color: var(--text-primary);
        }
        
        .stat-label {
          font-size: 14px;
          color: var(--text-muted);
        }
      }
    }
  }
  
  .hero-visual {
    position: relative;
    width: 400px;
    height: 400px;
    
    .floating-cards {
      position: relative;
      width: 100%;
      height: 100%;
    }
    
    .float-card {
      position: absolute;
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 24px;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      backdrop-filter: blur(10px);
      animation: float 6s infinite ease-in-out;
      
      .el-icon {
        font-size: 24px;
        color: var(--primary-color);
      }
      
      span {
        font-weight: 500;
        color: var(--text-primary);
      }
      
      &.card-1 {
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }
      
      &.card-2 {
        top: 30%;
        right: 5%;
        animation-delay: 1s;
      }
      
      &.card-3 {
        bottom: 30%;
        left: 5%;
        animation-delay: 2s;
      }
      
      &.card-4 {
        bottom: 10%;
        right: 10%;
        animation-delay: 3s;
      }
    }
  }
  
  .scroll-indicator {
    position: absolute;
    bottom: 40px;
    left: 50%;
    transform: translateX(-50%);
    color: var(--text-muted);
    animation: bounce 2s infinite;
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  40% {
    transform: translateX(-50%) translateY(-10px);
  }
  60% {
    transform: translateX(-50%) translateY(-5px);
  }
}

.content-section {
  padding: 80px 0;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 40px;
    
    &.center {
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    
    h2 {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 8px;
    }
    
    p {
      color: var(--text-secondary);
    }
  }
  
  .articles-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    
    .article-item {
      opacity: 0;
    }
  }
}

.features-section {
  padding: 80px 0;
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
  }
  
  .feature-card {
    padding: 32px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-lg);
    text-align: center;
    
    .feature-icon {
      width: 64px;
      height: 64px;
      margin: 0 auto 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--gradient-primary);
      border-radius: var(--radius-lg);
      
      .el-icon {
        font-size: 28px;
        color: white;
      }
    }
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 12px;
    }
    
    p {
      color: var(--text-secondary);
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

.sidebar-section {
  padding: 40px 0 80px;
  
  .content-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 40px;
  }
  
  .tags-cloud {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .tag-item {
      padding: 8px 16px;
      background: var(--bg-glass);
      border: 1px solid var(--border-color);
      border-radius: 9999px;
      color: var(--text-secondary);
      cursor: pointer;
      transition: all var(--transition-fast);
      
      &:hover {
        color: var(--primary-color);
        border-color: var(--primary-color);
        transform: scale(1.05);
      }
    }
  }
  
  .sidebar-card {
    padding: 24px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-lg);
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--border-color);
    }
    
    .category-list {
      .category-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        cursor: pointer;
        transition: all var(--transition-fast);
        
        &:hover {
          color: var(--primary-color);
        }
        
        .category-count {
          padding: 2px 8px;
          background: var(--bg-glass);
          border-radius: 9999px;
          font-size: 12px;
          color: var(--text-muted);
        }
      }
    }
  }
}
</style>
