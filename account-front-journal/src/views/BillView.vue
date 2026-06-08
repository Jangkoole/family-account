<template>
  <div class="bill-container">
    <!-- 页面标题 -->
    <div class="page-header-zh">
      <h1>收支记录管理</h1>
      <div class="header-actions">
        <el-button v-if="activeTab !== 'family'" type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新增记录
        </el-button>
        <el-button v-if="activeTab !== 'family'" class="btn-import" @click="openImportDialog">
          <el-icon><Upload /></el-icon> 导入记录
        </el-button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="bill-tabs" @tab-change="handleTabChange">
      <el-tab-pane name="my">
        <template #label>
          <span>我的记录</span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="family">
        <template #label>
          <span>家人记录</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="zh-card filter-card">
      <el-row :gutter="15" align="middle">
        <el-col :span="4">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-col>

        <el-col :span="3" v-if="activeTab === 'family'">
          <el-select v-model="filters.userId" placeholder="选择成员" clearable style="width: 100%">
            <el-option
              v-for="member in memberList"
              :key="member.userId"
              :label="member.nickname"
              :value="member.userId"
            />
          </el-select>
        </el-col>

        <el-col :span="3" v-if="activeTab === 'my'">
          <el-select v-model="filters.visible" placeholder="可见范围" clearable style="width: 100%">
            <el-option label="仅自己可见" value="PRIVATE" />
            <el-option label="家庭成员可见" value="FAMILY" />
          </el-select>
        </el-col>

        <el-col :span="3">
          <el-select v-model="filters.type" placeholder="类型" clearable style="width: 100%">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
        </el-col>

        <el-col :span="3">
          <el-select v-model="filters.categoryId" placeholder="分类" clearable style="width: 100%">
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-col>

        <el-col :span="4">
          <div class="amount-range">
            <el-input v-model.number="filters.minAmount" type="number" min="0" placeholder="最小" />
            <span class="range-sep">~</span>
            <el-input v-model.number="filters.maxAmount" type="number" min="0" placeholder="最大" />
          </div>
        </el-col>

        <el-col :span="3">
          <el-select v-model="filters.orderBy" placeholder="排序方式" clearable>
            <el-option label="日期降序" value="date_desc" />
            <el-option label="日期升序" value="date_asc" />
            <el-option label="金额降序" value="amount_desc" />
            <el-option label="金额升序" value="amount_asc" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <div class="button-group">
            <el-button type="primary" @click="searchBills">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 家人记录提示 -->
    <el-alert
      v-if="activeTab === 'family'"
      title="提示：家人记录仅显示设为'家庭成员可见'的收支记录"
      type="info"
      :closable="false"
      class="alert-tip"
    />

    <!-- 统计卡片 -->
    <div class="stat-mini-cards">
      <div class="stat-mini-card card-income">
        <div class="smc-label">总收入</div>
        <div class="smc-value income">¥{{ formatNumber(stats.totalIncome) }}</div>
      </div>
      <div class="stat-mini-card card-expense">
        <div class="smc-label">总支出</div>
        <div class="smc-value expense">¥{{ formatNumber(stats.totalExpense) }}</div>
      </div>
      <div class="stat-mini-card card-balance">
        <div class="smc-label">结余</div>
        <div class="smc-value" :class="stats.balance >= 0 ? 'income' : 'expense'">
          ¥{{ formatNumber(stats.balance) }}
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-card shadow="never" class="zh-card">
      <div v-if="activeTab === 'my' && selectedIds.length > 0" class="batch-toolbar">
        <el-button type="danger" size="small" @click="confirmBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除（{{ selectedIds.length }}）
        </el-button>
      </div>
      <el-table :data="billList" stripe fit @selection-change="handleSelectionChange" row-key="id" ref="tableRef">
        <el-table-column v-if="activeTab === 'my'" type="selection" width="40" :reserve-selection="true" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small" effect="light">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="activeTab === 'family'" label="成员" width="80" align="center" prop="nickname" />

        <el-table-column prop="categoryName" label="分类" width="80" align="center" />
        <el-table-column label="金额" align="center" width="120">
          <template #default="{ row }">
            <span :class="row.type === 'INCOME' ? 'income' : 'expense'" class="amount-display">
              ¥{{ formatNumber(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="120" align="center" />

        <el-table-column prop="note" label="备注" min-width="200" align="center" />

        <el-table-column label="可见范围" align="center" width="120">
          <template #default="{ row }">
            <el-tag :type="row.visible === 'FAMILY' ? 'info' : 'primary'" effect="light" class="visibility-tag">
              {{ row.visible === 'FAMILY' ? '家庭成员可见' : '仅自己可见' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="160" v-if="activeTab === 'my'">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" text @click="openEditDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="confirmDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑收支记录' : '新增收支记录'"
      width="550px"
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="类型" required>
          <el-radio-group v-model="formData.type">
            <el-radio value="EXPENSE">支出</el-radio>
            <el-radio value="INCOME">收入</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="分类" required>
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in filteredCategoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="金额" required>
          <el-input-number v-model="formData.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="日期" required>
          <el-date-picker
            v-model="formData.date"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="formData.note" type="textarea" rows="2" placeholder="可选" />
        </el-form-item>

        <el-form-item label="可见范围">
          <el-radio-group v-model="formData.visible">
            <el-radio value="PRIVATE">仅自己可见</el-radio>
            <el-radio value="FAMILY" :disabled="!userStore.familyInfo">家庭成员可见</el-radio>
          </el-radio-group>
          <div class="text-muted small">选择"家庭成员可见"后，家人可以在"家人记录"中看到此记录</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗（保持原有结构不变） -->
    <el-dialog v-model="importDialogVisible" title="导入收支记录" width="1100px" class="import-dialog">
      <div v-if="previewData.totalCount === 0">
        <el-form :model="importForm" label-width="100px">
          <el-form-item label="文件来源" required>
            <el-select v-model="importForm.source" placeholder="请选择文件来源" style="width: 100%">
              <el-option label="标准模板" value="SYSTEM" />
              <el-option label="微信账单" value="WECHAT" />
              <el-option label="支付宝账单" value="ALIPAY" />
            </el-select>
          </el-form-item>
          <el-form-item label="上传文件" required>
            <div style="display: flex; align-items: center; gap: 12px;">
              <el-upload
                class="upload-demo"
                ref="uploadRef"
                :auto-upload="false"
                :multiple="false"
                accept=".xlsx,.csv"
                :on-change="handleFileChange"
                :limit="1"
              >
                <el-button type="primary">选择文件</el-button>
              </el-upload>
              <span v-if="importForm.fileName" style="color: #4A7C59;">{{ importForm.fileName }}</span>
            </div>
            <div class="el-upload__tip" style="margin-left: 10px;">支持 .xlsx 和 .csv 格式文件</div>
          </el-form-item>
        </el-form>
      </div>

      <div v-else>
        <div style="margin-bottom: 15px;">
          <el-divider content-position="left">预览数据（共 {{ previewData.totalCount }} 条）</el-divider>
          <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
            以下是前5条数据的预览效果，确认后将导入全部数据
          </el-alert>
          <el-table :data="previewData.previewList" border stripe size="small" max-height="250">
            <el-table-column label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === 'INCOME' ? 'success' : row.type === 'EXPENSE' ? 'danger' : 'warning'" size="small" effect="light">
                  {{ row.type === 'INCOME' ? '收入' : row.type === 'EXPENSE' ? '支出' : '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="80" align="center" />
            <el-table-column label="金额" align="center">
              <template #default="{ row }">
                <span :class="row.type === 'INCOME' ? 'income' : 'expense'" class="amount-display">
                  ¥{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="日期" width="120" align="center" />
            <el-table-column prop="visible" label="可见范围" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.visible === 'FAMILY' ? 'primary' : row.visible === 'PRIVATE' ? 'info' : 'warning'" size="small" effect="light">
                  {{ row.visible === 'FAMILY' ? '家庭可见' : row.visible === 'PRIVATE' ? '仅自己可见' : '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注" min-width="120" align="center" />
          </el-table>
        </div>
        <el-alert type="warning" :closable="false">
          <strong>您确定要导入这些数据吗？</strong>
        </el-alert>
      </div>

      <template #footer>
        <el-button @click="closeImportDialog">取消</el-button>
        <el-button v-if="previewData.totalCount === 0" type="primary" disabled>请选择文件</el-button>
        <el-button v-else type="success" @click="handleConfirmImport" :loading="importLoading">
          确定导入（{{ previewData.totalCount }} 条）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { getBillList, addBill, updateBill, deleteBill, deleteBatchBill, getFamilyBillList, previewImport, confirmImport } from '@/api/bill'
import { getCategoryList } from '@/api/category'
import { getMemberList } from '@/api/family'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const billList = ref([])
const categoryList = ref([])
const memberList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const dateRange = ref([])
const activeTab = ref('my')

const filters = ref({
  userId: null,
  type: '',
  categoryId: '',
  visible: '',
  minAmount: '',
  maxAmount: '',
  orderBy: ''
})

const formData = ref({
  id: null,
  type: 'EXPENSE',
  categoryId: null,
  amount: null,
  date: new Date().toISOString().split('T')[0],
  note: '',
  visible: 'PRIVATE'
})

const isEdit = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)

const importDialogVisible = ref(false)
const importForm = ref({ source: 'SYSTEM', file: null, fileName: '' })
const previewData = ref({ previewId: '', totalCount: 0, previewList: [] })
const previewLoading = ref(false)
const importLoading = ref(false)
const uploadRef = ref(null)

const stats = computed(() => {
  const income = billList.value.filter(item => item.type === 'INCOME').reduce((sum, item) => sum + item.amount, 0)
  const expense = billList.value.filter(item => item.type === 'EXPENSE').reduce((sum, item) => sum + item.amount, 0)
  return { totalIncome: income, totalExpense: expense, balance: income - expense }
})

const filteredCategoryList = computed(() => {
  return categoryList.value.filter(cat => cat.type === formData.value.type)
})

const formatNumber = (num) => (num || 0).toFixed(2)

const handleTabChange = () => {
  page.value = 1
  filters.value.userId = null
  fetchBills()
}

const extractMembersFromBills = (bills) => {
  const memberMap = new Map()
  bills.forEach(bill => {
    if (bill.userId && bill.nickname) memberMap.set(bill.userId, { userId: bill.userId, nickname: bill.nickname })
  })
  return Array.from(memberMap.values())
}

const fetchMembers = async () => {
  try {
    const res = await getMemberList()
    if (res.code === 200) memberList.value = res.data
  } catch (error) { console.error('获取家庭成员失败', error) }
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) categoryList.value = res.data
  } catch (error) { console.error('获取分类失败', error) }
}

const fetchBills = async () => {
  const minVal = Number(filters.value.minAmount)
  const maxVal = Number(filters.value.maxAmount)
  if (filters.value.minAmount !== '' && filters.value.maxAmount !== '' && minVal > maxVal) {
    ElMessage.warning('最小金额不能大于最大金额')
    return
  }

  selectedIds.value = []
  try {
    let params = {
      page: page.value, pageSize: pageSize.value,
      startDate: dateRange.value?.[0], endDate: dateRange.value?.[1],
      type: filters.value.type, categoryId: filters.value.categoryId,
      visible: filters.value.visible,
      minAmount: filters.value.minAmount, maxAmount: filters.value.maxAmount,
      orderBy: filters.value.orderBy
    }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) delete params[key]
    })

    let res
    if (activeTab.value === 'family') {
      if (filters.value.userId) params.userId = filters.value.userId
      res = await getFamilyBillList(params)
    } else {
      res = await getBillList(params)
    }

    if (res.code === 200) {
      billList.value = res.data.list || []
      total.value = res.data.total || 0
      if (page.value === 1 && !window._isPaging) {
        ElMessage.success(`查询成功，共 ${total.value} 条记录`)
      }
      if (activeTab.value === 'family') {
        memberList.value = extractMembersFromBills(res.data.list || [])
        if (!res.data.list || res.data.list.length === 0) {
          ElMessageBox.alert('未查询到家庭成员的收支记录', '提示', { confirmButtonText: '确定', type: 'info' })
        }
      }
    } else {
      billList.value = []
      total.value = 0
      if (res.code === 400) {
        ElMessageBox.alert(res.message || '当前用户未加入任何家庭组', '提示', { confirmButtonText: '确定', type: 'info' })
      } else if (res.code === 403) {
        ElMessageBox.alert(res.message || '仅家庭管理员可查看成员记录明细', '提示', { confirmButtonText: '确定', type: 'warning' })
      } else {
        ElMessageBox.alert(res.message || '获取数据失败', '提示', { confirmButtonText: '确定', type: 'error' })
      }
    }
  } catch (error) {
    console.error('获取账单失败', error)
    ElMessage.error('获取账单失败')
  } finally { window._isPaging = false }
}

const searchBills = () => { page.value = 1; fetchBills() }

const scrollToTop = () => {
  const el = document.querySelector('.main-content') || document.documentElement
  el.scrollTo({ top: 0, behavior: 'smooth' })
}

const resetFilters = () => {
  filters.value = { userId: null, type: '', categoryId: '', visible: '', minAmount: '', maxAmount: '', orderBy: '' }
  dateRange.value = []
  page.value = 1
  fetchBills()
}

const handleSizeChange = (val) => { window._isPaging = true; pageSize.value = val; page.value = 1; scrollToTop(); fetchBills() }
const handleCurrentChange = (val) => { window._isPaging = true; page.value = val; scrollToTop(); fetchBills() }

const openAddDialog = () => {
  isEdit.value = false
  formData.value = { id: null, type: 'EXPENSE', categoryId: null, amount: null, date: new Date().toISOString().split('T')[0], note: '', visible: userStore.userInfo.defaultVisible || 'PRIVATE' }
  dialogVisible.value = true
}

const openEditDialog = (item) => {
  isEdit.value = true
  formData.value = { id: item.id, type: item.type, categoryId: item.categoryId, amount: item.amount, date: item.date, note: item.note || '', visible: item.visible || 'PRIVATE' }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.value.categoryId) { ElMessage.warning('请选择分类'); return }
  if (!formData.value.amount || formData.value.amount <= 0) { ElMessage.warning('请输入正确的金额'); return }
  submitting.value = true
  try {
    const data = { type: formData.value.type, categoryId: formData.value.categoryId, amount: formData.value.amount, date: formData.value.date, note: formData.value.note, visible: formData.value.visible }
    let res
    if (isEdit.value) { data.id = formData.value.id; res = await updateBill(data) }
    else { res = await addBill(data) }
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '添加成功'); dialogVisible.value = false; fetchBills() }
    else { ElMessage.error(res.message || '操作失败') }
  } catch (error) { console.error('提交失败', error); ElMessage.error('操作失败，请重试') }
  finally { submitting.value = false }
}

const confirmDelete = (item) => {
  ElMessageBox.confirm('确定要删除这条收支记录吗？', '删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteBill(item.id)
        if (res.code === 200) { ElMessage.success('删除成功'); fetchBills() }
        else { ElMessage.error(res.message || '删除失败') }
      } catch (error) { console.error('删除失败', error); ElMessage.error('删除失败，请重试') }
    }).catch(() => {})
}

const tableRef = ref(null)
const selectedIds = ref([])

const handleSelectionChange = (selection) => { selectedIds.value = selection.map(item => item.id) }

const confirmBatchDelete = () => {
  if (selectedIds.value.length === 0) return
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条收支记录吗？`, '批量删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteBatchBill(selectedIds.value)
        if (res.code === 200) { ElMessage.success(`成功删除 ${selectedIds.value.length} 条记录`); selectedIds.value = []; tableRef.value.clearSelection(); fetchBills() }
        else { ElMessage.error(res.message || '批量删除失败') }
      } catch (error) { console.error('批量删除失败', error); ElMessage.error('批量删除失败，请重试') }
    }).catch(() => {})
}

watch(() => formData.value.type, () => { formData.value.categoryId = null })

const openImportDialog = () => { previewData.value = { previewId: '', totalCount: 0, previewList: [] }; importForm.value = { source: 'SYSTEM', file: null, fileName: '' }; importDialogVisible.value = true }
const closeImportDialog = () => { importDialogVisible.value = false; previewData.value = { previewId: '', totalCount: 0, previewList: [] } }

const handleFileChange = async (uploadFile) => {
  const ext = uploadFile.name.split('.').pop().toLowerCase()
  if (ext !== 'xlsx' && ext !== 'csv') { ElMessage.error('仅支持 .xlsx 和 .csv 格式文件'); uploadRef.value.clearFiles(); return }
  importForm.value.file = uploadFile.raw; importForm.value.fileName = uploadFile.name
  if (!importForm.value.source) { ElMessage.error('请先选择文件来源'); uploadRef.value.clearFiles(); return }
  previewLoading.value = true
  try {
    const formData = new FormData(); formData.append('file', importForm.value.file); formData.append('source', importForm.value.source)
    const res = await previewImport(formData)
    if (res.code === 200) { previewData.value = { previewId: res.data.previewId, totalCount: res.data.totalCount, previewList: res.data.previewList } }
    else { ElMessage.error(res.message || '预览失败'); uploadRef.value.clearFiles(); importForm.value.fileName = '' }
  } catch (error) { ElMessage.error('预览失败：' + (error.message || '网络错误')); uploadRef.value.clearFiles(); importForm.value.fileName = '' }
  finally { previewLoading.value = false }
}

const handleConfirmImport = async () => {
  if (!previewData.value.previewId) { ElMessage.error('请先预览数据'); return }
  importLoading.value = true
  try {
    const res = await confirmImport(previewData.value.previewId)
    if (res.code === 200) {
      const { successCount, failCount, failReasons } = res.data
      let msg = `<span style="color: #4A7C59;">导入完成</span>！成功 ${successCount} 条，失败 ${failCount} 条`
      if (failCount > 0) { msg += `<br><br><span style="color: #C4342E;">失败原因</span><br>`; failReasons.forEach(item => { msg += `第 ${item.row} 行: ${item.reason}<br>` }) }
      ElMessageBox.alert(msg, '导入结果', { confirmButtonText: '确定', type: failCount > 0 ? 'warning' : 'success', dangerouslyUseHTMLString: true })
        .then(() => { closeImportDialog(); fetchBills() })
    } else { ElMessageBox.alert(res.message || '导入失败', '导入失败', { confirmButtonText: '确定', type: 'error' }) }
  } catch (error) { ElMessageBox.alert(error.message || '网络错误，请重试', '导入失败', { confirmButtonText: '确定', type: 'error' }) }
  finally { importLoading.value = false }
}

onMounted(() => { fetchCategories(); fetchMembers(); fetchBills() })
</script>

<style scoped>
.bill-container { width: 100%; }

/* 页面标题 */
.page-header-zh {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header-zh h1 {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-black);
  letter-spacing: 2px;
  margin: 0;
}

/* 标题装饰线 */
.page-header-zh::before {
  content: '';
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, var(--cinnabar) 0%, var(--gold) 100%);
  border-radius: 2px;
  margin-right: 12px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.btn-import {
  color: #4A7C59;
  border-color: #4A7C59;
  background: rgba(74, 124, 89, 0.05);
}
.btn-import:hover {
  color: #fff;
  background: #4A7C59;
  border-color: #4A7C59;
}

/* Tab */
.bill-tabs {
  margin-bottom: 16px;
}

/* 筛选卡片 */
.zh-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
}
.filter-card {
  margin-bottom: 16px;
}
.filter-card .el-card__body {
  padding: 20px;
}

.amount-range {
  display: flex;
  align-items: center;
  gap: 4px;
}
.range-sep {
  color: var(--text-secondary);
  flex-shrink: 0;
}

.button-group {
  display: flex;
  gap: 8px;
}

.alert-tip {
  margin-bottom: 16px;
}

/* 迷你统计卡片 */
.stat-mini-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-mini-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
  padding: 20px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
}

.stat-mini-card::after {
  content: '';
  position: absolute;
  right: -16px;
  top: -16px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  opacity: 0.06;
  pointer-events: none;
}
.card-income::after { background: #4A7C59; }
.card-expense::after { background: #C4342E; }
.card-balance::after { background: #B8860B; }

.stat-mini-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.smc-label {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.smc-value {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
}

.income { color: #4A7C59; }
.expense { color: #C4342E; }

/* 表格 */
.batch-toolbar {
  padding: 8px 0;
  margin-bottom: 8px;
}

.visibility-tag {
  min-width: 100px;
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-muted { color: var(--text-secondary); }
.small { font-size: 12px; }

/* 导入弹窗 */
:deep(.import-dialog) {
  margin-left: calc(17vw);
}
</style>
