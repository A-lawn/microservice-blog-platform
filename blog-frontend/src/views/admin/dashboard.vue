<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <component :is="stat.icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span class="value">{{ stat.value }}</span>
            <span class="trend" :class="stat.trend > 0 ? 'up' : 'down'">
              {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
            </span>
          </div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-chart">
          <svg viewBox="0 0 100 40" preserveAspectRatio="none">
            <path :d="stat.chartPath" fill="none" :stroke="stat.color" stroke-width="2"/>
          </svg>
        </div>
      </div>
    </div>
    
    <div class="charts-row">
      <div class="chart-card large">
        <div class="card-header">
          <h3>访问趋势</h3>
          <div class="time-filter">
            <button v-for="period in ['7天', '30天', '90天']" :key="period"
                    :class="{ active: selectedPeriod === period }"
                    @click="selectedPeriod = period">
              {{ period }}
            </button>
          </div>
        </div>
        <div class="chart-container">
          <div class="line-chart">
            <div class="chart-y-axis">
              <span v-for="i in 5" :key="i">{{ (5 - i) * 200 }}</span>
            </div>
            <div class="chart-area">
              <svg viewBox="0 0 400 150" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:#667eea;stop-opacity:0.3"/>
                    <stop offset="100%" style="stop-color:#667eea;stop-opacity:0"/>
                  </linearGradient>
                </defs>
                <path :d="areaPath" fill="url(#areaGradient)"/>
                <path :d="linePath" fill="none" stroke="#667eea" stroke-width="2"/>
                <circle v-for="(point, i) in chartPoints" :key="i"
                        :cx="point.x" :cy="point.y" r="4" fill="#667eea"/>
              </svg>
            </div>
          </div>
          <div class="chart-x-axis">
            <span v-for="day in chartLabels" :key="day">{{ day }}</span>
          </div>
        </div>
      </div>
      
      <div class="chart-card">
        <div class="card-header">
          <h3>内容分布</h3>
        </div>
        <div class="donut-chart">
          <svg viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="50" fill="none" stroke="#e2e8f0" stroke-width="12"/>
            <circle v-for="(segment, i) in donutSegments" :key="i"
                    cx="60" cy="60" r="50" fill="none"
                    :stroke="segment.color" stroke-width="12"
                    :stroke-dasharray="segment.dash"
                    :stroke-dashoffset="segment.offset"
                    transform="rotate(-90 60 60)"/>
          </svg>
          <div class="donut-center">
            <div class="donut-value">{{ totalArticles }}</div>
            <div class="donut-label">总文章</div>
          </div>
        </div>
        <div class="chart-legend">
          <div class="legend-item" v-for="item in legendItems" :key="item.label">
            <span class="legend-dot" :style="{ background: item.color }"></span>
            <span class="legend-label">{{ item.label }}</span>
            <span class="legend-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="data-row">
      <div class="data-card">
        <div class="card-header">
          <h3>最新文章</h3>
          <router-link to="/admin/articles" class="view-all">查看全部</router-link>
        </div>
        <div class="article-list">
          <div class="article-item" v-for="article in recentArticles" :key="article.id">
            <div class="article-cover" :style="{ background: article.cover || '#667eea' }"></div>
            <div class="article-info">
              <div class="article-title">{{ article.title }}</div>
              <div class="article-meta">
                <span>{{ article.author }}</span>
                <span>{{ article.date }}</span>
              </div>
            </div>
            <div class="article-stats">
              <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>{{ article.views }}</span>
              <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>{{ article.comments }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="data-card">
        <div class="card-header">
          <h3>活跃用户</h3>
          <router-link to="/admin/users" class="view-all">查看全部</router-link>
        </div>
        <div class="user-list">
          <div class="user-item" v-for="user in activeUsers" :key="user.id">
            <div class="user-avatar" :style="{ background: user.avatar }">{{ user.name.charAt(0) }}</div>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-role">{{ user.role }}</div>
            </div>
            <div class="user-status" :class="user.online ? 'online' : 'offline'">
              {{ user.online ? '在线' : '离线' }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="data-card">
        <div class="card-header">
          <h3>系统状态</h3>
        </div>
        <div class="system-status">
          <div class="status-item" v-for="service in systemStatus" :key="service.name">
            <div class="status-icon" :class="service.status">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path v-if="service.status === 'running'" d="M9 12l2 2 4-4"/>
              </svg>
            </div>
            <div class="status-info">
              <div class="status-name">{{ service.name }}</div>
              <div class="status-detail">{{ service.detail }}</div>
            </div>
            <div class="status-indicator" :class="service.status"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const selectedPeriod = ref('7天')

const stats = ref([
  { label: '总用户', value: '12,345', trend: 12.5, gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: '#667eea', icon: 'UsersIcon', chartPath: 'M0,30 Q25,20 50,25 T100,15' },
  { label: '文章数', value: '1,234', trend: 8.3, gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: '#f5576c', icon: 'FileIcon', chartPath: 'M0,25 Q25,30 50,20 T100,10' },
  { label: '评论数', value: '5,678', trend: -2.1, gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: '#4facfe', icon: 'CommentIcon', chartPath: 'M0,20 Q25,25 50,30 T100,25' },
  { label: '访问量', value: '89,012', trend: 15.8, gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', color: '#43e97b', icon: 'EyeIcon', chartPath: 'M0,35 Q25,25 50,20 T100,5' }
])

const chartPoints = ref([
  { x: 20, y: 100 }, { x: 70, y: 80 }, { x: 120, y: 90 },
  { x: 170, y: 60 }, { x: 220, y: 70 }, { x: 270, y: 40 },
  { x: 320, y: 50 }, { x: 370, y: 30 }
])

const chartLabels = ref(['周一', '周二', '周三', '周四', '周五', '周六', '周日'])

const linePath = computed(() => {
  return chartPoints.value.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')
})

const areaPath = computed(() => {
  const points = chartPoints.value
  const lastX = points[points.length - 1].x
  return `${linePath.value} L ${lastX} 150 L 20 150 Z`
})

const totalArticles = ref(1234)
const donutSegments = ref([
  { color: '#667eea', dash: '157 314', offset: 0 },
  { color: '#f5576c', dash: '94 314', offset: -157 },
  { color: '#4facfe', dash: '63 314', offset: -251 }
])

const legendItems = ref([
  { label: '技术文章', value: 456, color: '#667eea' },
  { label: '生活随笔', value: 289, color: '#f5576c' },
  { label: '其他', value: 489, color: '#4facfe' }
])

const recentArticles = ref([
  { id: 1, title: 'Vue3 组合式API最佳实践', author: '张三', date: '2小时前', views: 1234, comments: 56 },
  { id: 2, title: 'Spring Boot 微服务架构设计', author: '李四', date: '5小时前', views: 892, comments: 34 },
  { id: 3, title: 'Docker 容器化部署指南', author: '王五', date: '1天前', views: 567, comments: 23 },
  { id: 4, title: 'TypeScript 高级类型技巧', author: '赵六', date: '2天前', views: 445, comments: 18 }
])

const activeUsers = ref([
  { id: 1, name: '张三', role: '管理员', avatar: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', online: true },
  { id: 2, name: '李四', role: '编辑', avatar: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', online: true },
  { id: 3, name: '王五', role: '作者', avatar: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', online: false },
  { id: 4, name: '赵六', role: '作者', avatar: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', online: true }
])

const systemStatus = ref([
  { name: 'API Gateway', status: 'running', detail: '运行正常 · 99.99% 可用' },
  { name: 'User Service', status: 'running', detail: '运行正常 · 响应 23ms' },
  { name: 'Article Service', status: 'running', detail: '运行正常 · 响应 45ms' },
  { name: 'Database', status: 'running', detail: '连接池: 15/50' }
])

onMounted(async () => {
  try {
    const [usersRes, articlesRes] = await Promise.all([
      axios.get('/api/admin/users/statistics'),
      axios.get('/api/articles?page=0&size=5')
    ])
    
    if (usersRes.data?.data) {
      stats.value[0].value = usersRes.data.data.totalUsers?.toLocaleString() || '0'
    }
  } catch (e) {
    console.log('Dashboard data loading...')
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  }
  
  .dark-mode & {
    background: rgba(26, 26, 46, 0.9);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  
  svg { width: 28px; height: 28px; }
}

.stat-content {
  flex: 1;
  
  .stat-value {
    display: flex;
    align-items: baseline;
    gap: 8px;
    
    .value {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      
      .dark-mode & { color: #f1f5f9; }
    }
    
    .trend {
      font-size: 13px;
      font-weight: 600;
      padding: 2px 6px;
      border-radius: 6px;
      
      &.up {
        color: #10b981;
        background: rgba(16, 185, 129, 0.1);
      }
      
      &.down {
        color: #ef4444;
        background: rgba(239, 68, 68, 0.1);
      }
    }
  }
  
  .stat-label {
    font-size: 14px;
    color: #64748b;
    margin-top: 4px;
    
    .dark-mode & { color: #94a3b8; }
  }
}

.stat-chart {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 100px;
  height: 40px;
  opacity: 0.3;
  
  svg { width: 100%; height: 100%; }
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  
  .dark-mode & {
    background: rgba(26, 26, 46, 0.9);
  }
  
  &.large {
    .chart-container {
      height: 200px;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
    
    .dark-mode & { color: #f1f5f9; }
  }
  
  .view-all {
    font-size: 13px;
    color: #667eea;
    text-decoration: none;
    
    &:hover { text-decoration: underline; }
  }
}

.time-filter {
  display: flex;
  gap: 4px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 4px;
  
  .dark-mode & {
    background: rgba(255, 255, 255, 0.1);
  }
  
  button {
    padding: 6px 12px;
    border: none;
    border-radius: 6px;
    background: transparent;
    font-size: 12px;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s;
    
    .dark-mode & { color: #94a3b8; }
    
    &.active {
      background: white;
      color: #667eea;
      font-weight: 500;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      .dark-mode & {
        background: rgba(102, 126, 234, 0.2);
        color: #a5b4fc;
      }
    }
  }
}

.line-chart {
  display: flex;
  height: 150px;
  
  .chart-y-axis {
    width: 40px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    font-size: 11px;
    color: #94a3b8;
    padding-right: 8px;
  }
  
  .chart-area {
    flex: 1;
    
    svg { width: 100%; height: 100%; }
  }
}

.chart-x-axis {
  display: flex;
  justify-content: space-around;
  margin-top: 12px;
  padding-left: 40px;
  font-size: 12px;
  color: #94a3b8;
}

.donut-chart {
  width: 160px;
  height: 160px;
  margin: 0 auto 20px;
  position: relative;
  
  svg { width: 100%; height: 100%; }
  
  .donut-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    
    .donut-value {
      font-size: 24px;
      font-weight: 700;
      color: #1e293b;
      
      .dark-mode & { color: #f1f5f9; }
    }
    
    .donut-label {
      font-size: 12px;
      color: #64748b;
      
      .dark-mode & { color: #94a3b8; }
    }
  }
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .legend-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
  }
  
  .legend-label {
    flex: 1;
    font-size: 13px;
    color: #64748b;
    
    .dark-mode & { color: #94a3b8; }
  }
  
  .legend-value {
    font-size: 13px;
    font-weight: 600;
    color: #1e293b;
    
    .dark-mode & { color: #f1f5f9; }
  }
}

.data-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.data-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  
  .dark-mode & {
    background: rgba(26, 26, 46, 0.9);
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.2s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.03);
    
    .dark-mode & {
      background: rgba(255, 255, 255, 0.05);
    }
  }
  
  .article-cover {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    flex-shrink: 0;
  }
  
  .article-info {
    flex: 1;
    min-width: 0;
    
    .article-title {
      font-size: 14px;
      font-weight: 500;
      color: #1e293b;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      
      .dark-mode & { color: #f1f5f9; }
    }
    
    .article-meta {
      font-size: 12px;
      color: #94a3b8;
      margin-top: 4px;
      
      span { margin-right: 8px; }
    }
  }
  
  .article-stats {
    display: flex;
    gap: 12px;
    
    span {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #64748b;
      
      svg { width: 14px; height: 14px; }
    }
  }
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.2s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.03);
    
    .dark-mode & {
      background: rgba(255, 255, 255, 0.05);
    }
  }
  
  .user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 600;
    font-size: 14px;
  }
  
  .user-info {
    flex: 1;
    
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: #1e293b;
      
      .dark-mode & { color: #f1f5f9; }
    }
    
    .user-role {
      font-size: 12px;
      color: #94a3b8;
    }
  }
  
  .user-status {
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 6px;
    
    &.online {
      background: rgba(16, 185, 129, 0.1);
      color: #10b981;
    }
    
    &.offline {
      background: rgba(100, 116, 139, 0.1);
      color: #64748b;
    }
  }
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.02);
  
  .dark-mode & {
    background: rgba(255, 255, 255, 0.03);
  }
  
  .status-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.running {
      background: rgba(16, 185, 129, 0.1);
      color: #10b981;
    }
    
    svg { width: 20px; height: 20px; }
  }
  
  .status-info {
    flex: 1;
    
    .status-name {
      font-size: 14px;
      font-weight: 500;
      color: #1e293b;
      
      .dark-mode & { color: #f1f5f9; }
    }
    
    .status-detail {
      font-size: 12px;
      color: #94a3b8;
    }
  }
  
  .status-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    
    &.running {
      background: #10b981;
      box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
      animation: pulse 2s infinite;
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@media (max-width: 1400px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .data-row { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 1024px) {
  .charts-row { grid-template-columns: 1fr; }
  .data-row { grid-template-columns: 1fr; }
}
</style>
