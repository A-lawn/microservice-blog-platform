<template>
  <div class="comments-page">
    <div class="page-header">
      <h2>评论管理</h2>
      <div class="header-actions">
        <select v-model="statusFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="PENDING">待审核</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已拒绝</option>
        </select>
        <button class="btn-primary" @click="batchApprove" :disabled="!selectedIds.length">
          批量通过
        </button>
        <button class="btn-danger" @click="batchReject" :disabled="!selectedIds.length">
          批量拒绝
        </button>
      </div>
    </div>
    
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-value">{{ statistics.totalComments || 0 }}</span>
        <span class="stat-label">总评论</span>
      </div>
      <div class="stat-item pending">
        <span class="stat-value">{{ statistics.pendingComments || 0 }}</span>
        <span class="stat-label">待审核</span>
      </div>
      <div class="stat-item approved">
        <span class="stat-value">{{ statistics.approvedComments || 0 }}</span>
        <span class="stat-label">已通过</span>
      </div>
      <div class="stat-item rejected">
        <span class="stat-value">{{ statistics.rejectedComments || 0 }}</span>
        <span class="stat-label">已拒绝</span>
      </div>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>
              <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
            </th>
            <th>评论内容</th>
            <th>评论者</th>
            <th>文章</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="comment in comments" :key="comment.id">
            <td>
              <input type="checkbox" :value="comment.id" v-model="selectedIds" />
            </td>
            <td class="content-cell">
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-meta" v-if="comment.parentId">回复评论</div>
            </td>
            <td>
              <div class="user-cell">
                <div class="user-avatar">{{ comment.authorName?.charAt(0) || 'U' }}</div>
                <span>{{ comment.authorName }}</span>
              </div>
            </td>
            <td>
              <router-link :to="`/article/${comment.articleId}`" class="article-link">
                {{ comment.articleTitle || comment.articleId }}
              </router-link>
            </td>
            <td>
              <span class="status-badge" :class="comment.status.toLowerCase()">
                {{ getStatusText(comment.status) }}
              </span>
            </td>
            <td>{{ formatDate(comment.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button class="btn-icon approve" @click="approveComment(comment.id)" 
                        v-if="comment.status !== 'APPROVED'" title="通过">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </button>
                <button class="btn-icon reject" @click="rejectComment(comment.id)"
                        v-if="comment.status !== 'REJECTED'" title="拒绝">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
                <button class="btn-icon delete" @click="deleteComment(comment.id)" title="删除">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div class="empty-state" v-if="comments.length === 0">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <p>暂无评论数据</p>
      </div>
    </div>
    
    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="currentPage === 0" @click="currentPage--">上一页</button>
      <span>第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
      <button :disabled="currentPage >= totalPages - 1" @click="currentPage++">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'

interface Comment {
  id: string
  content: string
  articleId: string
  articleTitle?: string
  authorId: string
  authorName: string
  status: string
  parentId?: string
  likeCount: number
  createdAt: string
}

const comments = ref<Comment[]>([])
const statistics = ref({
  totalComments: 0,
  pendingComments: 0,
  approvedComments: 0,
  rejectedComments: 0
})
const selectedIds = ref<string[]>([])
const selectAll = ref(false)
const statusFilter = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 10

const fetchComments = async () => {
  try {
    const params = new URLSearchParams({
      page: currentPage.value.toString(),
      size: pageSize.toString()
    })
    if (statusFilter.value) {
      params.append('status', statusFilter.value)
    }
    
    const res = await axios.get(`/api/admin/comments?${params}`)
    if (res.data?.data) {
      comments.value = res.data.data.content || []
      totalPages.value = res.data.data.totalPages || 1
    }
  } catch (e) {
    console.error('Failed to fetch comments:', e)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await axios.get('/api/admin/comments/statistics')
    if (res.data?.data) {
      statistics.value = res.data.data
    }
  } catch (e) {
    console.error('Failed to fetch statistics:', e)
  }
}

const approveComment = async (id: string) => {
  try {
    await axios.put(`/api/admin/comments/${id}/status`, { status: 'APPROVED' })
    fetchComments()
    fetchStatistics()
  } catch (e) {
    console.error('Failed to approve comment:', e)
  }
}

const rejectComment = async (id: string) => {
  try {
    await axios.put(`/api/admin/comments/${id}/status`, { status: 'REJECTED' })
    fetchComments()
    fetchStatistics()
  } catch (e) {
    console.error('Failed to reject comment:', e)
  }
}

const deleteComment = async (id: string) => {
  if (!confirm('确定要删除这条评论吗？')) return
  
  try {
    await axios.delete(`/api/admin/comments/${id}`)
    fetchComments()
    fetchStatistics()
  } catch (e) {
    console.error('Failed to delete comment:', e)
  }
}

const batchApprove = async () => {
  if (!selectedIds.value.length) return
  
  try {
    await axios.post('/api/admin/comments/batch-approve', selectedIds.value)
    selectedIds.value = []
    selectAll.value = false
    fetchComments()
    fetchStatistics()
  } catch (e) {
    console.error('Failed to batch approve:', e)
  }
}

const batchReject = async () => {
  if (!selectedIds.value.length) return
  
  try {
    await axios.post('/api/admin/comments/batch-reject', selectedIds.value)
    selectedIds.value = []
    selectAll.value = false
    fetchComments()
    fetchStatistics()
  } catch (e) {
    console.error('Failed to batch reject:', e)
  }
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedIds.value = comments.value.map(c => c.id)
  } else {
    selectedIds.value = []
  }
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch([statusFilter, currentPage], () => {
  fetchComments()
})

onMounted(() => {
  fetchComments()
  fetchStatistics()
})
</script>

<style lang="scss" scoped>
.comments-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
    
    .dark-mode & { color: #f1f5f9; }
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 10px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  
  .dark-mode & {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.1);
    color: #f1f5f9;
  }
}

.btn-primary, .btn-danger {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  }
}

.btn-danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(245, 87, 108, 0.4);
  }
}

.stats-bar {
  display: flex;
  gap: 20px;
  
  .stat-item {
    flex: 1;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(20px);
    border-radius: 12px;
    padding: 16px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .dark-mode & {
      background: rgba(26, 26, 46, 0.9);
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      
      .dark-mode & { color: #f1f5f9; }
    }
    
    .stat-label {
      font-size: 13px;
      color: #64748b;
      margin-top: 4px;
      
      .dark-mode & { color: #94a3b8; }
    }
    
    &.pending .stat-value { color: #f59e0b; }
    &.approved .stat-value { color: #10b981; }
    &.rejected .stat-value { color: #ef4444; }
  }
}

.table-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  overflow: hidden;
  
  .dark-mode & {
    background: rgba(26, 26, 46, 0.9);
  }
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  
  th, td {
    padding: 16px;
    text-align: left;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    
    .dark-mode & {
      border-bottom-color: rgba(255, 255, 255, 0.05);
    }
  }
  
  th {
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;
    color: #64748b;
    background: rgba(0, 0, 0, 0.02);
    
    .dark-mode & {
      color: #94a3b8;
      background: rgba(255, 255, 255, 0.02);
    }
  }
  
  td {
    font-size: 14px;
    color: #1e293b;
    
    .dark-mode & { color: #f1f5f9; }
  }
  
  tbody tr:hover {
    background: rgba(0, 0, 0, 0.02);
    
    .dark-mode & {
      background: rgba(255, 255, 255, 0.02);
    }
  }
}

.content-cell {
  max-width: 300px;
  
  .comment-content {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .comment-meta {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
  }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
  }
}

.article-link {
  color: #667eea;
  text-decoration: none;
  
  &:hover { text-decoration: underline; }
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  
  &.pending {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
  }
  
  &.approved {
    background: rgba(16, 185, 129, 0.1);
    color: #10b981;
  }
  
  &.rejected {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-icon {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  
  svg {
    width: 16px;
    height: 16px;
  }
  
  &.approve {
    background: rgba(16, 185, 129, 0.1);
    color: #10b981;
    
    &:hover {
      background: rgba(16, 185, 129, 0.2);
    }
  }
  
  &.reject {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
    
    &:hover {
      background: rgba(245, 158, 11, 0.2);
    }
  }
  
  &.delete {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    
    &:hover {
      background: rgba(239, 68, 68, 0.2);
    }
  }
}

.empty-state {
  padding: 60px;
  text-align: center;
  color: #94a3b8;
  
  svg {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
  
  p { margin: 0; }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
  
  button {
    padding: 10px 20px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 10px;
    background: white;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
    
    .dark-mode & {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.1);
      color: #f1f5f9;
    }
    
    &:hover:not(:disabled) {
      background: #667eea;
      color: white;
      border-color: #667eea;
    }
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
  
  span {
    font-size: 14px;
    color: #64748b;
    
    .dark-mode & { color: #94a3b8; }
  }
}
</style>
