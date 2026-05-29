<template>
  <div class="bill-list">
    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" clearable placeholder="全部">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" clearable placeholder="全部分类">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="queryParams.minAmount" :min="0" placeholder="最小金额" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="queryParams.maxAmount" :min="0" placeholder="最大金额" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleAdd">+ 新增账单</el-button>
          <el-button @click="handleBatchVisible">批量修改可见范围</el-button>
          <el-button @click="handleImport">导入账单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 账单表格 -->
    <el-card>
      <el-table :data="billList" v-loading="loading" stripe border>
        <el-table-column type="selection" width="55" @selection-change="handleSelectionChange" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'INCOME' ? '#67C23A' : '#F56C6C' }">
              {{ row.type === 'INCOME' ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" show-overflow-tooltip />
        <el-table-column prop="visible" label="可见范围" width="110">
          <template #default="{ row }">
            <el-tag :type="row.visible === 'FAMILY' ? 'primary' : 'info'">
              {{ row.visible === 'FAMILY' ? '家庭成员可见' : '仅自己可见' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-dropdown @command="(cmd) => handleVisibleCommand(cmd, row)">
              <el-button link>可见范围</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="PRIVATE">仅自己可见</el-dropdown-item>
                  <el-dropdown-item command="FAMILY">家庭成员可见</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <BillForm ref="billFormRef" @success="getList" />

    <!-- 批量修改可见范围对话框 -->
    <VisibleBatchDialog ref="visibleBatchRef" :selected-ids="selectedIds" @success="getList" />

    <!-- 导入账单对话框 -->
    <ImportBill ref="importRef" @success="getList" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBills, deleteBill, updateVisible } from '@/api/bill'
import BillForm from './BillForm.vue'
import VisibleBatchDialog from '@/components/Bill/VisibleBatchDialog.vue'
import ImportBill from './ImportBill.vue'

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  startDate: null,
  endDate: null,
  type: null,
  categoryId: null,
  minAmount: null,
  maxAmount: null
})
const dateRange = ref([])
const total = ref(0)
const billList = ref([])
const loading = ref(false)
const selectedIds = ref([])

// 分类列表（从 API 获取）
const categoryList = ref([])

// 监听日期范围变化
watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    queryParams.startDate = newVal[0]
    queryParams.endDate = newVal[1]
  } else {
    queryParams.startDate = null
    queryParams.endDate = null
  }
  handleSearch()
})

// 获取账单列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listBills(queryParams)
    if (res.code === 200) {
      billList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取账单列表失败')
  } finally {
    loading.value = false
  }
}

// 加载分类（实际应调用 /category/list）
const loadCategories = async () => {
  // 临时模拟数据，替换为真实API
  categoryList.value = [
    { id: 1, name: '餐饮', type: 'EXPENSE' },
    { id: 2, name: '交通', type: 'EXPENSE' },
    { id: 9, name: '工资', type: 'INCOME' }
  ]
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置
const resetSearch = () => {
  queryParams.type = null
  queryParams.categoryId = null
  queryParams.minAmount = null
  queryParams.maxAmount = null
  dateRange.value = []
  handleSearch()
}

// 新增
const billFormRef = ref()
const handleAdd = () => {
  billFormRef.value.open()
}

// 编辑
const handleEdit = (row) => {
  billFormRef.value.open(row.id)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该账单吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteBill(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(res.message)
    }
  })
}

// 单条修改可见范围
const handleVisibleCommand = async (command, row) => {
  const res = await updateVisible({ ids: [row.id], visible: command })
  if (res.code === 200) {
    ElMessage.success('修改成功')
    getList()
  } else {
    ElMessage.error(res.message)
  }
}

// 表格多选
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量修改可见范围
const visibleBatchRef = ref()
const handleBatchVisible = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要修改的账单')
    return
  }
  visibleBatchRef.value.open()
}

// 导入
const importRef = ref()
const handleImport = () => {
  importRef.value.open()
}

onMounted(() => {
  loadCategories()
  getList()
})
</script>

<style scoped>
.bill-list {
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
</style>