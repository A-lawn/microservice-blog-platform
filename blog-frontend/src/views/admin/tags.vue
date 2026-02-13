<template>
  <div class="admin-tags">
    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon> 新增标签
      </el-button>
    </div>
    
    <el-table :data="tags" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="slug" label="Slug" />
      <el-table-column prop="articleCount" label="文章数" width="100" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" @click="showDialog(row)">编辑</el-button>
          <el-button text type="danger" @click="deleteTag(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新增标签'" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="60px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="Slug" prop="slug">
          <el-input v-model="form.slug" placeholder="URL友好标识" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi } from '@/api'
import type { Tag } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tags = ref<Tag[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: null as number | null,
  name: '',
  slug: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
}

onMounted(() => {
  loadTags()
})

const loadTags = async () => {
  loading.value = true
  try {
    tags.value = await articleApi.getTags()
  } finally {
    loading.value = false
  }
}

const showDialog = (tag?: Tag) => {
  if (tag) {
    isEdit.value = true
    form.id = tag.id
    form.name = tag.name
    form.slug = tag.slug || ''
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.slug = ''
  }
  dialogVisible.value = true
}

const saveTag = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  saving.value = true
  try {
    await articleApi.createTag(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadTags()
  } finally {
    saving.value = false
  }
}

const deleteTag = async (tag: Tag) => {
  try {
    await ElMessageBox.confirm('确定要删除该标签吗？', '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    loadTags()
  } catch {}
}
</script>

<style lang="scss" scoped>
.admin-tags {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
