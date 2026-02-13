<template>
  <div class="admin-categories">
    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon> 新增分类
      </el-button>
    </div>
    
    <el-table :data="categories" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="articleCount" label="文章数" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" @click="showDialog(row)">编辑</el-button>
          <el-button text type="danger" @click="deleteCategory(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="60px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi } from '@/api'
import type { Category } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: null as number | null,
  name: '',
  description: '',
  sortOrder: 0,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

onMounted(() => {
  loadCategories()
})

const loadCategories = async () => {
  loading.value = true
  try {
    categories.value = await articleApi.getCategories()
  } finally {
    loading.value = false
  }
}

const showDialog = (category?: Category) => {
  if (category) {
    isEdit.value = true
    form.id = category.id
    form.name = category.name
    form.description = category.description || ''
    form.sortOrder = 0
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.description = ''
    form.sortOrder = 0
  }
  dialogVisible.value = true
}

const saveCategory = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await articleApi.updateCategory(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await articleApi.createCategory(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCategories()
  } finally {
    saving.value = false
  }
}

const deleteCategory = async (category: Category) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
    await articleApi.deleteCategory(category.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch {}
}
</script>

<style lang="scss" scoped>
.admin-categories {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
