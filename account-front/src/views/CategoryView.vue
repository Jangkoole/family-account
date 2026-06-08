<template>
  <div class="category-container">
    <div class="page-header-zh">
      <h1>收支分类管理</h1>
      <div class="header-actions">
        <el-button class="btn-income" @click="openAddDialog('INCOME')">
          <el-icon><Plus /></el-icon> 添加收入分类
        </el-button>
        <el-button class="btn-expense" @click="openAddDialog('EXPENSE')">
          <el-icon><Plus /></el-icon> 添加支出分类
        </el-button>
      </div>
    </div>

    <div class="category-columns">
      <!-- 收入分类 -->
      <el-card shadow="never" class="zh-card category-card">
        <template #header>
          <div class="card-header-zh">
            <div class="ch-left">
              <span class="header-dot income-dot"></span>
              <span>收入分类</span>
            </div>
            <el-tag size="small" type="success" effect="light" round>
              {{ incomeCategories.length }} 项
            </el-tag>
          </div>
        </template>
        <div v-if="incomeCategories.length === 0" class="empty-hint">
          <div class="empty-icon">—</div>
          <span>暂无收入分类</span>
        </div>
        <div v-for="cat in incomeCategories" :key="cat.id" class="category-item">
          <div class="category-info">
            <span class="category-dot income-dot-bg"></span>
            <span class="category-name">{{ cat.name }}</span>
            <el-tag v-if="cat.isSystem" size="small" type="info" effect="plain" round>系统</el-tag>
            <el-tag v-else size="small" effect="light" round class="custom-tag">自定义</el-tag>
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

      <!-- 支出分类 -->
      <el-card shadow="never" class="zh-card category-card">
        <template #header>
          <div class="card-header-zh">
            <div class="ch-left">
              <span class="header-dot expense-dot"></span>
              <span>支出分类</span>
            </div>
            <el-tag size="small" type="danger" effect="light" round>
              {{ expenseCategories.length }} 项
            </el-tag>
          </div>
        </template>
        <div v-if="expenseCategories.length === 0" class="empty-hint">
          <div class="empty-icon">—</div>
          <span>暂无支出分类</span>
        </div>
        <div v-for="cat in expenseCategories" :key="cat.id" class="category-item">
          <div class="category-info">
            <span class="category-dot expense-dot-bg"></span>
            <span class="category-name">{{ cat.name }}</span>
            <el-tag v-if="cat.isSystem" size="small" type="info" effect="plain" round>系统</el-tag>
            <el-tag v-else size="small" effect="light" round class="custom-tag">自定义</el-tag>
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
    </div>

    <!-- 待审核申请 -->
    <el-card v-if="userStore.familyRole === 'ADMIN' && applyList.length > 0" shadow="never" class="zh-card apply-card">
      <template #header>
        <div class="card-header-zh">
          <div class="ch-left">
            <span class="header-dot warn-dot"></span>
            <span>待审核分类申请</span>
          </div>
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

    <!-- 新增/修改对话框 -->
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
            <el-radio value="INCOME">收入</el-radio>
            <el-radio value="EXPENSE">支出</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
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
            <el-option v-for="cat in sameTypeCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getCategoryList, addCategory, updateCategory, deleteCategory,
  getCategoryApplyList, reviewCategoryApply
} from '@/api/category'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const categoryList = ref([])
const applyList = ref([])

const incomeCategories = computed(() => categoryList.value.filter(c => c.type === 'INCOME'))
const expenseCategories = computed(() => categoryList.value.filter(c => c.type === 'EXPENSE'))

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const formData = ref({ id: null, name: '', type: 'EXPENSE' })
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }]
}

const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewForm = ref({ applyId: null, categoryName: '', type: '', mergeToCategoryId: null, targetCategoryId: null })

const sameTypeCategories = computed(() => {
  return categoryList.value.filter(cat => cat.type === reviewForm.value.type)
})

async function fetchCategories() {
  const res = await getCategoryList()
  if (res.code === 200) categoryList.value = res.data
}

async function fetchApplyList() {
  const res = await getCategoryApplyList()
  if (res.code === 200) applyList.value = res.data
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
      if (isEdit.value) res = await updateCategory({ id: formData.value.id, name: formData.value.name })
      else res = await addCategory({ name: formData.value.name, type: formData.value.type })
      if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; await fetchCategories() }
      else ElMessage.error(res.message)
    } finally { submitting.value = false }
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除分类"${row.name}"？删除前需确保该分类下没有关联的收支记录`, '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
  const res = await deleteCategory(row.id)
  if (res.code === 200) { ElMessage.success('删除成功'); await fetchCategories() }
  else ElMessage.error(res.message)
}

function handleApproveApply(row) {
  reviewForm.value = { applyId: row.applyId, categoryName: row.categoryName, type: row.type, mergeToCategoryId: null, targetCategoryId: null }
  reviewDialogVisible.value = true
}

async function handleRejectApply(applyId) {
  await ElMessageBox.confirm('确认拒绝该分类申请？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
  const res = await reviewCategoryApply({ applyId, approve: false })
  if (res.code === 200) { ElMessage.success('已拒绝'); await fetchApplyList() }
  else ElMessage.error(res.message)
}

async function handleConfirmReview() {
  if (reviewForm.value.mergeToCategoryId === 0 && !reviewForm.value.targetCategoryId) { ElMessage.warning('请选择要合并到的目标分类'); return }
  reviewLoading.value = true
  try {
    const mergeToCategoryId = reviewForm.value.mergeToCategoryId === 0 ? reviewForm.value.targetCategoryId : null
    const res = await reviewCategoryApply({ applyId: reviewForm.value.applyId, approve: true, mergeToCategoryId })
    if (res.code === 200) { ElMessage.success('审核通过'); reviewDialogVisible.value = false; await fetchApplyList(); await fetchCategories() }
    else ElMessage.error(res.message)
  } finally { reviewLoading.value = false }
}

onMounted(async () => {
  await fetchCategories()
  if (userStore.familyRole === 'ADMIN') await fetchApplyList()
})
</script>

<style scoped>
.category-container { max-width: 1200px; margin: 0 auto; }

.page-header-zh {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.page-header-zh h1 {
  font-family: var(--font-display); font-size: 22px; font-weight: 700; color: var(--ink-black); letter-spacing: 2px; margin: 0;
}
.page-header-zh::before {
  content: ''; width: 4px; height: 24px; background: linear-gradient(180deg, var(--cinnabar) 0%, var(--gold) 100%); border-radius: 2px; margin-right: 12px;
}

.header-actions { display: flex; gap: 8px; }

.btn-income { color: #4A7C59; border-color: #4A7C59; background: rgba(74, 124, 89, 0.05); }
.btn-income:hover { color: #fff; background: #4A7C59; border-color: #4A7C59; }

.btn-expense { color: #C4342E; border-color: #C4342E; background: rgba(196, 52, 46, 0.04); }
.btn-expense:hover { color: #fff; background: #C4342E; border-color: #C4342E; }

.zh-card { background: #fffdf8; border: 1px solid var(--gold-pale); border-radius: var(--radius-md); }

.category-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }

.card-header-zh { display: flex; align-items: center; justify-content: space-between; }
.ch-left { display: flex; align-items: center; gap: 10px; }
.header-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.income-dot { background: #4A7C59; }
.expense-dot { background: #C4342E; }
.warn-dot { background: #E6A23C; }

.category-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 4px; border-bottom: 1px solid var(--gold-pale);
  transition: background-color 0.2s;
}
.category-item:hover { background-color: rgba(184, 134, 11, 0.04); }
.category-item:last-child { border-bottom: none; }

.category-info { display: flex; align-items: center; gap: 10px; }
.category-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.income-dot-bg { background: #4A7C59; }
.expense-dot-bg { background: #C4342E; }
.category-name { font-size: 14px; color: var(--text-primary); }

.custom-tag { background: rgba(184, 134, 11, 0.1); border-color: #C9A96E; color: #8B6914; }

.category-actions { display: flex; gap: 2px; opacity: 0; transition: opacity 0.2s; }
.category-item:hover .category-actions { opacity: 1; }

.empty-hint {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  color: var(--text-secondary); font-size: 13px; padding: 32px 0;
}
.empty-icon { font-size: 24px; color: var(--gold-pale); font-family: var(--font-display); }

.apply-card { margin-top: 20px; }

@media (max-width: 768px) { .category-columns { grid-template-columns: 1fr; } }
</style>
