<template>
  <div class="category-container">
    <!-- 页面标题 -->
    <div class="page-header mb-4">
      <div class="header-left">
        <h1>收支分类管理</h1>
      </div>
      <div class="header-right">
        <el-button type="success" plain @click="openAddDialog('INCOME')" style="--el-button-text-color: #3d7a22; --el-button-border-color: #3d7a22; --el-button-bg-color: #e8f5e0;">
          <el-icon><Plus /></el-icon> 添加收入分类
        </el-button>
        <el-button type="danger" plain @click="openAddDialog('EXPENSE')">
          <el-icon><Plus /></el-icon> 添加支出分类
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 收入分类 -->
      <el-col :span="12">
        <el-card shadow="hover" class="category-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="card-icon income-icon"><Top /></el-icon>
                收入分类
              </span>
              <el-tag size="small" type="success" effect="light" round>
                {{ incomeCategories.length }} 项
              </el-tag>
            </div>
          </template>
          <div v-if="incomeCategories.length === 0" class="empty-hint">
            <el-icon class="empty-icon"><FolderDelete /></el-icon>
            <span>暂无收入分类</span>
          </div>
          <div v-for="cat in incomeCategories" :key="cat.id" class="category-item">
            <div class="category-info">
              <el-icon class="category-dot income-dot"><CircleCheck /></el-icon>
              <span class="category-name">{{ cat.name }}</span>
              <el-tag v-if="cat.isSystem" size="small" type="info" effect="plain" round>系统</el-tag>
              <el-tag v-else size="small" type="warning" effect="light" round>自定义</el-tag>
            </div>
            <div v-if="!cat.isSystem" class="category-actions">
              <el-button text size="small" @click="openEditDialog(cat)">
                <el-icon><Edit /></el-icon> 修改
              </el-button>
              <el-button text size="small" type="danger" @click="handleDelete(cat)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 支出分类 -->
      <el-col :span="12">
        <el-card shadow="hover" class="category-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="card-icon expense-icon"><Bottom /></el-icon>
                支出分类
              </span>
              <el-tag size="small" type="danger" effect="light" round>
                {{ expenseCategories.length }} 项
              </el-tag>
            </div>
          </template>
          <div v-if="expenseCategories.length === 0" class="empty-hint">
            <el-icon class="empty-icon"><FolderDelete /></el-icon>
            <span>暂无支出分类</span>
          </div>
          <div v-for="cat in expenseCategories" :key="cat.id" class="category-item">
            <div class="category-info">
              <el-icon class="category-dot expense-dot"><CircleCheck /></el-icon>
              <span class="category-name">{{ cat.name }}</span>
              <el-tag v-if="cat.isSystem" size="small" type="info" effect="plain" round>系统</el-tag>
              <el-tag v-else size="small" type="warning" effect="light" round>自定义</el-tag>
            </div>
            <div v-if="!cat.isSystem" class="category-actions">
              <el-button text size="small" @click="openEditDialog(cat)">
                <el-icon><Edit /></el-icon> 修改
              </el-button>
              <el-button text size="small" type="danger" @click="handleDelete(cat)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 家庭管理员：待审核分类申请 -->
    <el-card v-if="userStore.familyRole === 'ADMIN' && applyList.length > 0" shadow="hover" class="apply-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="card-icon warning-icon"><WarningFilled /></el-icon>
            待审核分类申请
          </span>
          <el-tag size="small" type="warning" effect="light" round>{{ applyList.length }} 条</el-tag>
        </div>
      </template>
      <el-table :data="applyList" stripe size="small">
        <el-table-column label="申请人" prop="nickname" width="120" />
        <el-table-column label="分类名称" prop="categoryName" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small" effect="light">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="applyTime" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleApproveApply(row)">通过</el-button>
            <el-button size="small" @click="handleRejectApply(row.applyId)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/修改分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改分类' : '新增分类'"
      width="420px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="收支类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio value="INCOME">
              <el-icon style="vertical-align: middle; color: #67C23A;"><Top /></el-icon>
              收入
            </el-radio>
            <el-radio value="EXPENSE">
              <el-icon style="vertical-align: middle; color: #F56C6C;"><Bottom /></el-icon>
              支出
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 审核通过对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核分类申请"
      width="420px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="reviewForm" ref="reviewFormRef" label-width="100px">
        <el-form-item label="分类名称">
          <el-tag :type="reviewForm.type === 'INCOME' ? 'success' : 'danger'" effect="light" size="large">
            {{ reviewForm.categoryName }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="reviewForm.mergeToCategoryId">
            <el-radio :label="null">作为独立分类新增</el-radio>
            <el-radio :label="0">合并到已有分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="reviewForm.mergeToCategoryId === 0" label="目标分类">
          <el-select v-model="reviewForm.targetCategoryId" placeholder="请选择目标分类" style="width: 100%">
            <el-option
              v-for="cat in sameTypeCategories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="handleConfirmReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Top, Bottom, WarningFilled, Plus, Edit, Delete, CircleCheck, FolderDelete } from '@element-plus/icons-vue'
import {
  getCategoryList, addCategory, updateCategory, deleteCategory,
  getCategoryApplyList, reviewCategoryApply
} from '@/api/category'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const categoryList = ref([])
const applyList = ref([])

// 按类型分组
const incomeCategories = computed(() => categoryList.value.filter(c => c.type === 'INCOME'))
const expenseCategories = computed(() => categoryList.value.filter(c => c.type === 'EXPENSE'))

// 新增/修改
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const formData = ref({
  id: null,
  name: '',
  type: 'EXPENSE'
})
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }]
}

// 审核
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewForm = ref({
  applyId: null,
  categoryName: '',
  type: '',
  mergeToCategoryId: null,
  targetCategoryId: null
})

const sameTypeCategories = computed(() => {
  return categoryList.value.filter(cat => cat.type === reviewForm.value.type)
})

async function fetchCategories() {
  const res = await getCategoryList()
  if (res.code === 200) {
    categoryList.value = res.data
  }
}

async function fetchApplyList() {
  const res = await getCategoryApplyList()
  if (res.code === 200) {
    applyList.value = res.data
  }
}

function openAddDialog(type) {
  isEdit.value = false
  formData.value = { id: null, name: '', type }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  formData.value = { id: row.id, name: row.name, type: row.type }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let res
      if (isEdit.value) {
        res = await updateCategory({ id: formData.value.id, name: formData.value.name })
      } else {
        res = await addCategory({ name: formData.value.name, type: formData.value.type })
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        dialogVisible.value = false
        await fetchCategories()
      } else {
        ElMessage.error(res.message)
      }
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确认删除分类"${row.name}"？删除前需确保该分类下没有关联的收支记录`,
    '提示',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  )
  const res = await deleteCategory(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    await fetchCategories()
  } else {
    ElMessage.error(res.message)
  }
}

function handleApproveApply(row) {
  reviewForm.value = {
    applyId: row.applyId,
    categoryName: row.categoryName,
    type: row.type,
    mergeToCategoryId: null,
    targetCategoryId: null
  }
  reviewDialogVisible.value = true
}

async function handleRejectApply(applyId) {
  await ElMessageBox.confirm('确认拒绝该分类申请？', '提示', {
    confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning'
  })
  const res = await reviewCategoryApply({ applyId, approve: false })
  if (res.code === 200) {
    ElMessage.success('已拒绝')
    await fetchApplyList()
  } else {
    ElMessage.error(res.message)
  }
}

async function handleConfirmReview() {
  if (reviewForm.value.mergeToCategoryId === 0 && !reviewForm.value.targetCategoryId) {
    ElMessage.warning('请选择要合并到的目标分类')
    return
  }
  reviewLoading.value = true
  try {
    const mergeToCategoryId = reviewForm.value.mergeToCategoryId === 0
      ? reviewForm.value.targetCategoryId
      : null
    const res = await reviewCategoryApply({
      applyId: reviewForm.value.applyId,
      approve: true,
      mergeToCategoryId
    })
    if (res.code === 200) {
      ElMessage.success('审核通过')
      reviewDialogVisible.value = false
      await fetchApplyList()
      await fetchCategories()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    reviewLoading.value = false
  }
}

onMounted(async () => {
  await fetchCategories()
  if (userStore.familyRole === 'ADMIN') {
    await fetchApplyList()
  }
})
</script>

<style scoped>
.category-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面标题 — 与 BillView 保持一致 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin-bottom: 20px;
  min-height: 40px;
}
.page-header h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}
.header-right {
  display: flex;
  gap: 8px;
}

/* 分类卡片 */
.category-card {
  margin-bottom: 0;
}
.category-card .el-card__body {
  padding: 8px 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 500;
}
.card-icon {
  font-size: 18px;
}
.income-icon { color: #67C23A; }
.expense-icon { color: #F56C6C; }
.warning-icon { color: #E6A23C; }

/* 分类列表项 */
.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 4px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}
.category-item:hover {
  background-color: #fafafa;
}
.category-item:last-child {
  border-bottom: none;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.category-dot {
  font-size: 8px;
}
.income-dot { color: #67C23A; }
.expense-dot { color: #F56C6C; }
.category-name {
  font-size: 14px;
  color: #303133;
}

.category-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}
.category-item:hover .category-actions {
  opacity: 1;
}

/* 空状态 */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #c0c4cc;
  font-size: 13px;
  padding: 32px 0;
}
.empty-icon {
  font-size: 32px;
}

/* 审核申请卡片 */
.apply-card {
  margin-top: 20px;
}

/* 对话框中的图标 */
.el-radio .el-icon {
  margin-right: 2px;
}
</style>
