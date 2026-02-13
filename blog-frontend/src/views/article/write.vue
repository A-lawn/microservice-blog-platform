<template>
  <div class="write-page">
    <div class="write-container">
      <header class="write-header">
        <div class="header-content">
          <div class="header-left">
            <router-link to="/" class="back-link">
              <el-icon><ArrowLeft /></el-icon>
              <span>返回首页</span>
            </router-link>
            <div class="title-wrapper">
              <h1 class="write-title">{{ isEdit ? '编辑文章' : '撰写新文章' }}</h1>
              <p class="write-subtitle">用文字记录思想，用代码改变世界</p>
            </div>
          </div>
          <div class="header-actions">
            <button class="action-btn draft-btn" @click="saveDraft" :disabled="saving">
              <el-icon v-if="!saving"><Document /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
              <span>{{ saving ? '保存中...' : '保存草稿' }}</span>
            </button>
            <button class="action-btn publish-btn" @click="publishArticle" :disabled="publishing">
              <el-icon v-if="!publishing"><Promotion /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
              <span>{{ publishing ? '发布中...' : (form.status === 'PUBLISHED' ? '更新文章' : '发布文章') }}</span>
            </button>
          </div>
        </div>
      </header>

      <main class="write-main">
        <div class="editor-wrapper">
          <div class="editor-header">
            <input
              v-model="form.title"
              type="text"
              class="article-title-input"
              placeholder="请输入文章标题..."
              maxlength="200"
            />
            <div class="title-meta">
              <span class="char-count">{{ form.title.length }}/200 字符</span>
              <span class="status-badge" :class="form.status.toLowerCase()">{{ statusText }}</span>
            </div>
          </div>

          <div class="editor-body">
            <div class="toolbar">
              <div class="toolbar-group">
                <button class="tool-btn" @click="insertMarkdown('**', '**')" title="粗体 (Ctrl+B)">
                  <strong>B</strong>
                </button>
                <button class="tool-btn" @click="insertMarkdown('*', '*')" title="斜体 (Ctrl+I)">
                  <em>I</em>
                </button>
                <button class="tool-btn" @click="insertMarkdown('~~', '~~')" title="删除线">
                  <s>S</s>
                </button>
              </div>
              <div class="toolbar-divider"></div>
              <div class="toolbar-group">
                <button class="tool-btn" @click="insertMarkdown('# ', '')" title="一级标题">H1</button>
                <button class="tool-btn" @click="insertMarkdown('## ', '')" title="二级标题">H2</button>
                <button class="tool-btn" @click="insertMarkdown('### ', '')" title="三级标题">H3</button>
                <button class="tool-btn" @click="insertMarkdown('#### ', '')" title="四级标题">H4</button>
              </div>
              <div class="toolbar-divider"></div>
              <div class="toolbar-group">
                <button class="tool-btn" @click="insertMarkdown('- ', '')" title="无序列表">
                  <el-icon><List /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('1. ', '')" title="有序列表">
                  <el-icon><Finished /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('- [ ] ', '')" title="任务列表">
                  <el-icon><Select /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('> ', '')" title="引用">
                  <el-icon><ChatQuote /></el-icon>
                </button>
              </div>
              <div class="toolbar-divider"></div>
              <div class="toolbar-group">
                <button class="tool-btn" @click="insertMarkdown('```\n', '\n```')" title="代码块">
                  <el-icon><Document /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('`', '`')" title="行内代码">
                  <el-icon><CaretRight /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('---\n', '')" title="分割线">
                  <el-icon><Minus /></el-icon>
                </button>
              </div>
              <div class="toolbar-divider"></div>
              <div class="toolbar-group">
                <button class="tool-btn" @click="insertMarkdown('[', '](url)')" title="链接">
                  <el-icon><Link /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('![alt](', ')')" title="图片">
                  <el-icon><Picture /></el-icon>
                </button>
                <button class="tool-btn" @click="insertMarkdown('\n| 列1 | 列2 |\n| --- | --- |\n| 内容 | 内容 |', '')" title="表格">
                  <el-icon><Grid /></el-icon>
                </button>
              </div>
            </div>

            <div class="editor-panes">
              <div class="pane write-pane">
                <div class="pane-header">
                  <div class="pane-indicator write"></div>
                  <span>Markdown 编辑</span>
                  <div class="pane-stats">
                    <span>{{ wordCount }} 字</span>
                    <span>{{ readTime }} 分钟阅读</span>
                  </div>
                </div>
                <textarea
                  ref="editorRef"
                  v-model="form.content"
                  class="editor-textarea"
                  placeholder="开始写作吧... 支持 Markdown 语法

快捷键：
- Ctrl+B 粗体
- Ctrl+I 斜体
- Ctrl+K 插入链接"
                  @keydown.ctrl.b.prevent="insertMarkdown('**', '**')"
                  @keydown.ctrl.i.prevent="insertMarkdown('*', '*')"
                  @keydown.ctrl.k.prevent="insertMarkdown('[', '](url)')"
                ></textarea>
              </div>
              <div class="pane preview-pane">
                <div class="pane-header">
                  <div class="pane-indicator preview"></div>
                  <span>实时预览</span>
                </div>
                <div class="preview-content" v-html="renderedContent"></div>
              </div>
            </div>
          </div>
        </div>

        <aside class="settings-sidebar">
          <div class="sidebar-section">
            <div class="section-header">
              <el-icon><Setting /></el-icon>
              <span>文章设置</span>
            </div>
            
            <div class="setting-item">
              <label class="setting-label">文章摘要</label>
              <textarea
                v-model="form.summary"
                class="setting-textarea"
                placeholder="输入文章摘要，帮助读者快速了解内容..."
                rows="4"
                maxlength="300"
              ></textarea>
              <span class="setting-count">{{ form.summary.length }}/300</span>
            </div>

            <div class="setting-item">
              <label class="setting-label">选择分类</label>
              <div class="category-select" @click="showCategoryDropdown = !showCategoryDropdown">
                <span class="selected-value" v-if="form.categoryId">
                  {{ getCategoryName(form.categoryId) }}
                </span>
                <span class="placeholder" v-else>选择一个分类</span>
                <el-icon class="select-arrow"><ArrowDown /></el-icon>
              </div>
              <div class="category-dropdown" v-show="showCategoryDropdown">
                <div
                  class="category-option"
                  v-for="category in articleStore.categories"
                  :key="category.id"
                  :class="{ active: form.categoryId === category.id }"
                  @click="selectCategory(category.id)"
                >
                  <span>{{ category.name }}</span>
                  <el-icon v-if="form.categoryId === category.id"><Check /></el-icon>
                </div>
              </div>
            </div>

            <div class="setting-item">
              <label class="setting-label">文章标签</label>
              <div class="tags-wrapper">
                <div class="tags-list">
                  <span
                    v-for="(tag, index) in form.tags"
                    :key="index"
                    class="tag-chip"
                  >
                    {{ tag }}
                    <el-icon class="tag-remove" @click="removeTag(index)"><Close /></el-icon>
                  </span>
                </div>
                <input
                  v-model="tagInput"
                  type="text"
                  class="tag-input"
                  placeholder="输入标签后按回车添加..."
                  @keydown.enter.prevent="addTag"
                  @keydown.tab.prevent="addTag"
                />
              </div>
              <div class="suggested-tags" v-if="articleStore.tags.length">
                <span class="suggested-label">热门标签：</span>
                <span
                  v-for="tag in suggestedTags"
                  :key="tag.id"
                  class="suggested-tag"
                  :class="{ added: form.tags.includes(tag.name) }"
                  @click="addSuggestedTag(tag.name)"
                >
                  {{ tag.name }}
                </span>
              </div>
            </div>

            <div class="setting-item">
              <label class="setting-label">封面图片</label>
              <div class="cover-uploader" @click="triggerCoverUpload">
                <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
                <div v-else class="cover-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <span>点击上传封面</span>
                  <span class="upload-hint">支持 JPG、PNG，最大 5MB</span>
                </div>
              </div>
              <input
                ref="coverInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleCoverUpload"
              />
            </div>
          </div>

          <div class="sidebar-section stats-section">
            <div class="section-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>文章统计</span>
            </div>
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon words">
                  <el-icon><Edit /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ wordCount }}</span>
                  <span class="stat-label">总字数</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon time">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ readTime }}</span>
                  <span class="stat-label">分钟阅读</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon paragraphs">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ paragraphCount }}</span>
                  <span class="stat-label">段落数</span>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { useArticleStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const editorRef = ref<HTMLTextAreaElement>()
const coverInputRef = ref<HTMLInputElement>()
const saving = ref(false)
const publishing = ref(false)
const tagInput = ref('')
const showCategoryDropdown = ref(false)

const DRAFT_STORAGE_KEY = 'article_draft'

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  coverImage: '',
  summary: '',
  content: '',
  categoryId: null as number | null,
  tags: [] as string[],
  status: 'DRAFT',
})

const statusText = computed(() => {
  switch (form.status) {
    case 'DRAFT': return '草稿'
    case 'PUBLISHED': return '已发布'
    case 'ARCHIVED': return '已归档'
    default: return '未知'
  }
})

const renderedContent = computed(() => {
  if (!form.content) {
    return `<div class="empty-preview">
      <el-icon><Edit /></el-icon>
      <p>开始输入内容，预览将在这里显示...</p>
    </div>`
  }
  const html = marked(form.content)
  return DOMPurify.sanitize(html)
})

const wordCount = computed(() => {
  return form.content.replace(/\s/g, '').length
})

const readTime = computed(() => {
  return Math.max(1, Math.ceil(wordCount.value / 400))
})

const paragraphCount = computed(() => {
  return form.content.split(/\n\s*\n/).filter(p => p.trim()).length
})

const suggestedTags = computed(() => {
  return articleStore.tags.slice(0, 10)
})

const getCategoryName = (id: number) => {
  const category = articleStore.categories.find(c => c.id === id)
  return category?.name || ''
}

const selectCategory = (id: number) => {
  form.categoryId = id
  showCategoryDropdown.value = false
}

const saveDraftToStorage = () => {
  if (isEdit.value) return
  const draft = {
    title: form.title,
    coverImage: form.coverImage,
    summary: form.summary,
    content: form.content,
    categoryId: form.categoryId,
    tags: form.tags,
    savedAt: new Date().toISOString()
  }
  localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(draft))
}

const loadDraftFromStorage = () => {
  if (isEdit.value) return
  const draftStr = localStorage.getItem(DRAFT_STORAGE_KEY)
  if (draftStr) {
    try {
      const draft = JSON.parse(draftStr)
      if (draft.title || draft.content) {
        form.title = draft.title || ''
        form.coverImage = draft.coverImage || ''
        form.summary = draft.summary || ''
        form.content = draft.content || ''
        form.categoryId = draft.categoryId || null
        form.tags = draft.tags || []
        ElMessage.success('已恢复上次未保存的草稿')
      }
    } catch (e) {
      console.error('Failed to load draft:', e)
    }
  }
}

const clearDraftFromStorage = () => {
  localStorage.removeItem(DRAFT_STORAGE_KEY)
}

watch([() => form.title, () => form.content, () => form.categoryId, () => form.tags], () => {
  if (form.title || form.content) {
    saveDraftToStorage()
  }
}, { deep: true })

onMounted(async () => {
  await articleStore.fetchCategories()
  await articleStore.fetchTags()
  
  if (isEdit.value) {
    const article = await articleStore.fetchArticleById(route.params.id as string)
    if (article) {
      form.title = article.title
      form.coverImage = article.coverImage || ''
      form.summary = article.summary || ''
      form.content = article.content
      form.categoryId = article.category?.id || null
      form.tags = article.tags || []
      form.status = article.status
    }
  } else {
    loadDraftFromStorage()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdowns)
})

const closeDropdowns = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.category-select') && !target.closest('.category-dropdown')) {
    showCategoryDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdowns)
})

const insertMarkdown = (prefix: string, suffix: string) => {
  const textarea = editorRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)
  const replacement = prefix + selectedText + suffix
  
  form.content = form.content.substring(0, start) + replacement + form.content.substring(end)
  
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selectedText.length)
  }, 0)
}

const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !form.tags.includes(tag)) {
    form.tags.push(tag)
  }
  tagInput.value = ''
}

const removeTag = (index: number) => {
  form.tags.splice(index, 1)
}

const addSuggestedTag = (tagName: string) => {
  if (!form.tags.includes(tagName)) {
    form.tags.push(tagName)
  }
}

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverUpload = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await fetch('/api/files/upload', {
      method: 'POST',
      body: formData,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const data = await response.json()
    if (data.code === 200) {
      form.coverImage = data.data.url
      ElMessage.success('封面上传成功')
    }
  } catch (e) {
    ElMessage.error('封面上传失败')
  }
}

const saveDraft = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  
  saving.value = true
  try {
    if (isEdit.value) {
      await articleStore.updateArticle(route.params.id as string, form)
    } else {
      await articleStore.createArticle(form)
    }
    clearDraftFromStorage()
    ElMessage.success('草稿保存成功')
  } finally {
    saving.value = false
  }
}

const publishArticle = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }
  
  publishing.value = true
  try {
    let articleId: string
    if (isEdit.value) {
      await articleStore.updateArticle(route.params.id as string, form)
      articleId = route.params.id as string
    } else {
      const article = await articleStore.createArticle(form)
      articleId = article.id
    }
    
    if (form.status !== 'PUBLISHED') {
      await articleStore.publishArticle(articleId)
    }
    
    clearDraftFromStorage()
    ElMessage.success(isEdit.value ? '文章更新成功' : '文章发布成功')
    router.push(`/article/${articleId}`)
  } finally {
    publishing.value = false
  }
}
</script>

<style lang="scss" scoped>
.write-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.write-container {
  max-width: 1600px;
  margin: 0 auto;
}

.write-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  backdrop-filter: blur(20px);
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
  }
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 24px;
  }
  
  .back-link {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    color: var(--text-secondary);
    background: var(--bg-glass);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    font-size: 14px;
    transition: all var(--transition-fast);
    
    &:hover {
      color: var(--primary-color);
      border-color: var(--primary-color);
      background: rgba(99, 102, 241, 0.1);
    }
  }
  
  .title-wrapper {
    .write-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .write-subtitle {
      font-size: 13px;
      color: var(--text-muted);
      margin: 4px 0 0;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
    
    .action-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 20px;
      font-size: 14px;
      font-weight: 500;
      border-radius: var(--radius-md);
      border: none;
      cursor: pointer;
      transition: all var(--transition-fast);
      
      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
      
      .is-loading {
        animation: spin 1s linear infinite;
      }
    }
    
    .draft-btn {
      color: var(--text-primary);
      background: var(--bg-glass);
      border: 1px solid var(--border-color);
      
      &:hover:not(:disabled) {
        background: var(--bg-tertiary);
        border-color: var(--text-muted);
      }
    }
    
    .publish-btn {
      color: white;
      background: var(--gradient-primary);
      box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
      
      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
      }
    }
  }
}

.write-main {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
  padding: 24px;
  
  @media (max-width: 1200px) {
    grid-template-columns: 1fr;
  }
}

.editor-wrapper {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.editor-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
  
  .article-title-input {
    width: 100%;
    font-size: 32px;
    font-weight: 700;
    color: var(--text-primary);
    background: transparent;
    border: none;
    outline: none;
    padding: 0;
    margin-bottom: 12px;
    
    &::placeholder {
      color: var(--text-muted);
    }
  }
  
  .title-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .char-count {
      font-size: 13px;
      color: var(--text-muted);
    }
    
    .status-badge {
      padding: 4px 12px;
      font-size: 12px;
      font-weight: 500;
      border-radius: 9999px;
      
      &.draft {
        background: rgba(245, 158, 11, 0.15);
        color: #f59e0b;
      }
      
      &.published {
        background: rgba(16, 185, 129, 0.15);
        color: #10b981;
      }
      
      &.archived {
        background: rgba(100, 116, 139, 0.15);
        color: #64748b;
      }
    }
  }
}

.editor-body {
  .toolbar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 4px;
    padding: 12px 16px;
    background: var(--bg-tertiary);
    border-bottom: 1px solid var(--border-color);
    
    .toolbar-group {
      display: flex;
      gap: 2px;
    }
    
    .toolbar-divider {
      width: 1px;
      height: 24px;
      background: var(--border-color);
      margin: 0 8px;
    }
    
    .tool-btn {
      width: 34px;
      height: 34px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: var(--text-secondary);
      background: transparent;
      border: none;
      border-radius: var(--radius-sm);
      cursor: pointer;
      transition: all var(--transition-fast);
      
      &:hover {
        color: var(--primary-color);
        background: rgba(99, 102, 241, 0.1);
      }
    }
  }
}

.editor-panes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 600px;
  
  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    
    .preview-pane {
      display: none;
    }
  }
}

.pane {
  display: flex;
  flex-direction: column;
  
  &:first-child {
    border-right: 1px solid var(--border-color);
  }
  
  .pane-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border-color);
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    
    .pane-indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      
      &.write {
        background: var(--primary-color);
        box-shadow: 0 0 8px var(--primary-color);
      }
      
      &.preview {
        background: var(--success-color);
        box-shadow: 0 0 8px var(--success-color);
      }
    }
    
    .pane-stats {
      margin-left: auto;
      display: flex;
      gap: 16px;
      font-size: 12px;
      font-weight: 400;
      color: var(--text-muted);
    }
  }
}

.editor-textarea {
  flex: 1;
  padding: 20px;
  font-family: 'JetBrains Mono', 'Monaco', 'Menlo', monospace;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  background: var(--bg-primary);
  border: none;
  outline: none;
  resize: none;
  
  &::placeholder {
    color: var(--text-muted);
    line-height: 1.6;
  }
}

.preview-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  color: var(--text-primary);
  line-height: 1.8;
  font-size: 15px;
  
  .empty-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--text-muted);
    text-align: center;
    
    .el-icon {
      font-size: 48px;
      margin-bottom: 16px;
      opacity: 0.3;
    }
    
    p {
      margin: 0;
    }
  }
  
  :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
    margin: 24px 0 16px;
    color: var(--text-primary);
    font-weight: 700;
  }
  
  :deep(h1) { font-size: 28px; border-bottom: 2px solid var(--border-color); padding-bottom: 12px; }
  :deep(h2) { font-size: 24px; }
  :deep(h3) { font-size: 20px; }
  :deep(h4) { font-size: 18px; }
  
  :deep(p) {
    margin-bottom: 16px;
  }
  
  :deep(pre) {
    background: var(--bg-tertiary);
    padding: 16px;
    border-radius: var(--radius-md);
    overflow-x: auto;
    margin: 16px 0;
    border: 1px solid var(--border-color);
  }
  
  :deep(code) {
    background: var(--bg-tertiary);
    padding: 3px 8px;
    border-radius: 4px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 14px;
  }
  
  :deep(pre code) {
    background: transparent;
    padding: 0;
  }
  
  :deep(blockquote) {
    border-left: 4px solid var(--primary-color);
    padding: 12px 20px;
    margin: 16px 0;
    background: var(--bg-glass);
    border-radius: 0 var(--radius-md) var(--radius-md) 0;
    color: var(--text-secondary);
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 24px;
    margin: 16px 0;
  }
  
  :deep(li) {
    margin-bottom: 8px;
  }
  
  :deep(a) {
    color: var(--primary-color);
    text-decoration: none;
    border-bottom: 1px dashed var(--primary-color);
    
    &:hover {
      border-bottom-style: solid;
    }
  }
  
  :deep(img) {
    max-width: 100%;
    border-radius: var(--radius-md);
    margin: 16px 0;
  }
  
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    
    th, td {
      padding: 12px;
      border: 1px solid var(--border-color);
      text-align: left;
    }
    
    th {
      background: var(--bg-secondary);
      font-weight: 600;
    }
  }
  
  :deep(hr) {
    border: none;
    height: 2px;
    background: var(--border-color);
    margin: 24px 0;
  }
}

.settings-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  
  @media (max-width: 1200px) {
    order: -1;
  }
}

.sidebar-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 16px 20px;
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border-color);
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    
    .el-icon {
      color: var(--primary-color);
    }
  }
}

.setting-item {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  
  &:last-child {
    border-bottom: none;
  }
  
  .setting-label {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: var(--text-secondary);
    margin-bottom: 10px;
  }
  
  .setting-textarea {
    width: 100%;
    padding: 12px;
    font-size: 14px;
    color: var(--text-primary);
    background: var(--bg-glass);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    resize: none;
    outline: none;
    transition: border-color var(--transition-fast);
    
    &:focus {
      border-color: var(--primary-color);
    }
    
    &::placeholder {
      color: var(--text-muted);
    }
  }
  
  .setting-count {
    display: block;
    text-align: right;
    font-size: 12px;
    color: var(--text-muted);
    margin-top: 6px;
  }
}

.category-select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--bg-glass);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: border-color var(--transition-fast);
  
  &:hover {
    border-color: var(--primary-color);
  }
  
  .selected-value {
    color: var(--text-primary);
  }
  
  .placeholder {
    color: var(--text-muted);
  }
  
  .select-arrow {
    color: var(--text-muted);
    transition: transform var(--transition-fast);
  }
}

.category-dropdown {
  margin-top: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  max-height: 200px;
  overflow-y: auto;
  
  .category-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    cursor: pointer;
    transition: background var(--transition-fast);
    
    &:hover {
      background: var(--bg-glass);
    }
    
    &.active {
      color: var(--primary-color);
      background: rgba(99, 102, 241, 0.1);
    }
  }
}

.tags-wrapper {
  background: var(--bg-glass);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 10px;
  transition: border-color var(--transition-fast);
  
  &:focus-within {
    border-color: var(--primary-color);
  }
  
  .tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;
  }
  
  .tag-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    font-size: 12px;
    color: var(--primary-color);
    background: rgba(99, 102, 241, 0.1);
    border-radius: 9999px;
    
    .tag-remove {
      cursor: pointer;
      opacity: 0.6;
      font-size: 12px;
      
      &:hover {
        opacity: 1;
      }
    }
  }
  
  .tag-input {
    width: 100%;
    padding: 4px;
    font-size: 14px;
    color: var(--text-primary);
    background: transparent;
    border: none;
    outline: none;
    
    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.suggested-tags {
  margin-top: 12px;
  
  .suggested-label {
    font-size: 12px;
    color: var(--text-muted);
  }
  
  .suggested-tag {
    display: inline-block;
    padding: 4px 10px;
    font-size: 12px;
    color: var(--text-secondary);
    background: var(--bg-glass);
    border: 1px solid var(--border-color);
    border-radius: 9999px;
    margin: 4px;
    cursor: pointer;
    transition: all var(--transition-fast);
    
    &:hover {
      color: var(--primary-color);
      border-color: var(--primary-color);
    }
    
    &.added {
      color: var(--success-color);
      border-color: var(--success-color);
      background: rgba(16, 185, 129, 0.1);
    }
  }
}

.cover-uploader {
  width: 100%;
  height: 180px;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: border-color var(--transition-fast);
  
  &:hover {
    border-color: var(--primary-color);
    
    .cover-placeholder {
      color: var(--primary-color);
    }
  }
  
  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--text-muted);
    
    .upload-icon {
      font-size: 36px;
      margin-bottom: 8px;
    }
    
    .upload-hint {
      font-size: 12px;
      margin-top: 4px;
      opacity: 0.6;
    }
  }
}

.stats-section {
  .stats-grid {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px 20px;
  }
  
  .stat-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px;
    background: var(--bg-glass);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color);
    
    .stat-icon {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--radius-md);
      font-size: 20px;
      
      &.words {
        background: rgba(99, 102, 241, 0.15);
        color: var(--primary-color);
      }
      
      &.time {
        background: rgba(16, 185, 129, 0.15);
        color: var(--success-color);
      }
      
      &.paragraphs {
        background: rgba(236, 72, 153, 0.15);
        color: var(--secondary-color);
      }
    }
    
    .stat-info {
      .stat-value {
        display: block;
        font-size: 22px;
        font-weight: 700;
        color: var(--text-primary);
      }
      
      .stat-label {
        font-size: 12px;
        color: var(--text-muted);
      }
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
