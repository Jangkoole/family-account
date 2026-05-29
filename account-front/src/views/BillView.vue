<template>
  <div class="bill-container">
    <!-- 页面标题 -->
    <div class="page-header mb-4">
      <div class="header-left">
        <h2>收支记录管理</h2>
        <p class="text-muted">管理你的收支记录，查看家庭成员的共享记录</p>
      </div>
      <div class="header-right">
        <el-button v-if="activeTab !== 'family'" type="primary" size="small" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新增记录
        </el-button>
        <el-button v-if="activeTab !== 'family'" type="success" size="small" @click="openImportDialog" style="margin-left: 8px;">
          <el-icon><Upload /></el-icon> 导入记录
        </el-button>
      </div>
    </div>

    <!-- Tab 切换：我的记录 / 家人记录 -->
    <el-tabs v-model="activeTab" class="mb-4" @tab-change="handleTabChange">
      <el-tab-pane name="my">
        <template #label>
          <span class="iconfont icon-wodezhangdan"></span> 我的记录
        </template>
      </el-tab-pane>
      <el-tab-pane name="family">
        <template #label>
          <span class="iconfont icon-jiaren"></span> 家人记录
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 筛选栏 -->
    <el-card class="mb-4">
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
        
        <!-- 家人记录时才显示成员筛选，我的记录时显示可见范围筛选 -->
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
        
        <!-- 金额范围 -->
        <el-col :span="4">
          <div class="amount-range" style="display: flex; align-items: center;">
            <el-input v-model="filters.minAmount" type="number" placeholder="最小" style="width: 45%" />
            <span style="margin: 0 4px;">~</span>
            <el-input v-model="filters.maxAmount" type="number" placeholder="最大" style="width: 45%" />
          </div>
        </el-col>
        
        <!-- 排序 -->
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
            <el-button type="primary" @click="searchBills"><span class="iconfont icon-chaxun"></span> 查询</el-button>
            <el-button @click="resetFilters"><span class="iconfont icon-zhongzhi"></span> 重置</el-button>
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
      class="mb-4"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">总收入</div>
            <div class="stat-value text-success">¥{{ formatNumber(stats.totalIncome) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">总支出</div>
            <div class="stat-value text-danger">¥{{ formatNumber(stats.totalExpense) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">结余</div>
            <div class="stat-value" :class="stats.balance >= 0 ? 'text-success' : 'text-danger'">
              ¥{{ formatNumber(stats.balance) }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="billList" stripe fit>
        <el-table-column label="类型" width="80" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 家人记录时显示成员列 -->
        <el-table-column v-if="activeTab === 'family'" label="成员" width="80" align="center" header-align="center" prop="nickname" />
        
        <el-table-column prop="categoryName" label="分类" width="70" align="center" header-align="center" />
        <el-table-column label="金额" align="center" header-align="center">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'INCOME' ? '#67c23a' : '#f56c6c' }">
              ¥{{ formatNumber(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="120" align="center" header-align="center" />
        
        <el-table-column prop="note" label="备注" min-width="200" align="center" header-align="center" />
        
        <el-table-column label="可见范围" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 'FAMILY' ? 'info' : 'primary'" class="visibility-tag">
              {{ row.visible === 'FAMILY' ? '家庭成员可见' : '仅自己可见' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" align="center" header-align="center" v-if="activeTab === 'my'">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="openEditDialog(row)"><span class="iconfont icon-bianji"></span> 编辑</el-button>
              <el-button type="danger" size="small" @click="confirmDelete(row)"><span class="iconfont icon-shanchu"></span> 删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗（增加标签选择） -->
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
            <el-radio value="FAMILY">家庭成员可见</el-radio>
          </el-radio-group>
          <div class="text-muted small">选择"家庭成员可见"后，家人可以在"家人记录"中看到此记录</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="导入收支记录" width="1100px">
      <!-- 文件选择区域（预览前显示） -->
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
              <span v-if="importForm.fileName" style="color: #67c23a;">{{ importForm.fileName }}</span>
            </div>
            <div class="el-upload__tip" style="margin-left: 10px;">支持 .xlsx 和 .csv 格式文件</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 预览数据区域（选择文件后显示） -->
      <div v-else>
        <div style="margin-bottom: 15px;">
          <el-divider content-position="left">预览数据（共 {{ previewData.totalCount }} 条）</el-divider>
          <el-alert type="info" :closable="false" style="margin-bottom: 15px;">
            以下是前5条数据的预览效果，确认后将导入全部数据
          </el-alert>
          <el-table :data="previewData.previewList" border stripe size="small" max-height="250">
            <el-table-column label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === 'INCOME' ? 'success' : row.type === 'EXPENSE' ? 'danger' : 'warning'" size="small">
                  {{ row.type === 'INCOME' ? '收入' : row.type === 'EXPENSE' ? '支出' : '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="80" align="center" />
            <el-table-column label="金额" align="center">
              <template #default="{ row }">
                <span :style="{ color: row.type === 'INCOME' ? '#67c23a' : '#f56c6c' }">
                  ¥{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="日期" width="120" align="center" />
            <el-table-column prop="visible" label="可见范围" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.visible === 'FAMILY' ? 'primary' : row.visible === 'PRIVATE' ? 'info' : 'warning'" size="small">
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

      <!-- 导入说明按钮 -->
      <el-divider />
      <div class="import-help">
        <el-button type="text" @click="showHelp = !showHelp" style="padding: 0;">
          {{ showHelp ? '▼ 收起导入说明' : '▶ 查看导入说明' }}
        </el-button>

        <div v-if="showHelp" style="margin-top: 15px;">
          <h4 style="margin-bottom: 10px; font-size: calc(1rem + 0.3em); font-weight: 600; padding: 10px 14px; background: rgb(115 148 177); opacity: 0.8; color: #fff; border-radius: 4px;">
            <span class="iconfont icon-daorugeshi_upload" style="font-size: 21px; margin-right: 8px;"></span>
            导入格式说明
          </h4>
          <p style="font-size: 13px; color: #444; margin-bottom: 10px; padding: 12px; border-left: 3px solid #1a252f;">
            <strong style="color: #1a252f;">列顺序（必须严格按照此顺序）：</strong><br>
            <span style="color: #27ae60;">1.</span> 日期（必填）- 格式：YYYY-MM-DD<br>
            <span style="color: #27ae60;">2.</span> 类型（必填）- INCOME（收入）/ EXPENSE（支出）<br>
            <span style="color: #27ae60;">3.</span> 分类名称（必填）- 需与系统分类一致<br>
            <span style="color: #27ae60;">4.</span> 金额（必填）- 必须大于0<br>
            <span style="color: #7f8c8d;">5.</span> 可见范围（选填）- PRIVATE（仅自己可见）/ FAMILY（家庭成员可见），默认PRIVATE<br>
            <span style="color: #7f8c8d;">6.</span> 备注（选填）- 描述信息
          </p>

          <h4 style="margin-bottom: 10px; font-size: calc(1rem + 0.3em); font-weight: 600; padding: 10px 14px; background: rgb(125 169 115); opacity: 0.8; color: #fff; border-radius: 4px;">
            <span class="iconfont icon-xiazaishilishuju" style="font-size: 21px; margin-right: 8px;"></span>
            下载示例数据
          </h4>
          <div style="max-height: 180px; overflow-y: auto; border: 1px solid #ccc; border-radius: 8px; background: #ffffff;">
            <table style="width: 100%; font-size: 12px; border-collapse: collapse;">
              <thead>
                <tr style="background: #e8e8e8;">
                  <th style="border: 1px solid #ccc; padding: 6px; text-align: center; color: #1a252f; font-weight: 600;">日期</th>
                  <th style="border: 1px solid #ccc; padding: 6px; text-align: center; color: #1a252f; font-weight: 600;">类型</th>
                  <th style="border: 1px solid #ccc; padding: 6px; text-align: center; color: #1a252f; font-weight: 600;">分类名称</th>
                  <th style="border: 1px solid #ccc; padding: 6px; text-align: center; color: #1a252f; font-weight: 600;">金额</th>
                  <th style="border: 1px solid #ccc; padding: 6px; text-align: center; color: #1a252f; font-weight: 600;">可见范围</th>
                  <th style="border: 1px solid #ccc; padding: 6px; text-align: center; color: #1a252f; font-weight: 600;">备注</th>
                </tr>
              </thead>
              <tbody>
                <tr style="background: #fff;"><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">2026-05-26</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center; color: #c0392b;">EXPENSE</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">餐饮</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">25.50</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">PRIVATE</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">午餐外卖</td></tr>
                <tr style="background: #f5f5f5;"><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">2026-05-25</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center; color: #1e8449;">INCOME</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">工资</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">12000.00</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">FAMILY</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">五月工资</td></tr>
                <tr style="background: #fff;"><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">2026-05-24</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center; color: #c0392b;">EXPENSE</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">购物</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">156.00</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">PRIVATE</td><td style="border: 1px solid #ccc; padding: 4px; text-align: center;">日用品</td></tr>
              </tbody>
            </table>
          </div>

          <h4 style="margin-bottom: 10px; margin-top: 10px; font-size: calc(1rem + 0.3em); font-weight: 600; padding: 10px 14px; background: rgb(183 122 165); opacity: 0.8; color: #fff; border-radius: 4px;">
            <span class="iconfont icon-fenlei" style="font-size: 21px; margin-right: 8px;"></span>
            分类
          </h4>
          <div style="font-size: 12px; columns: 2; gap: 15px;">
            <div style="padding: 10px; border-left: 3px solid #c0392b;">
              <strong style="color: #a93226;">支出：</strong><br>
              餐饮、交通、购物、住房、医疗、教育、娱乐、其他支出
            </div>
            <div style="padding: 10px; border-left: 3px solid #1e8449;">
              <strong style="color: #145a32;">收入：</strong><br>
              工资、奖金、理财收益、其他收入
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeImportDialog">取消</el-button>
        <el-button v-if="previewData.totalCount === 0" type="primary" disabled>
          请选择文件
        </el-button>
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
import { Plus, Collection, Upload } from '@element-plus/icons-vue'
import { getBillList, addBill, updateBill, deleteBill, getFamilyBillList, previewImport, confirmImport } from '@/api/bill'
import { getCategoryList } from '@/api/category'
import { getMemberList } from '@/api/family'

// 数据
const billList = ref([])
const categoryList = ref([])
const memberList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const dateRange = ref([])
const activeTab = ref('my')  // my: 我的记录, family: 家人记录

// 筛选条件
const filters = ref({
  userId: null,      // 家人记录时筛选指定成员
  type: '',
  categoryId: '',
  visible: '',
  minAmount: '',     // 最小金额
  maxAmount: '',     // 最大金额
  orderBy: ''        // 排序方式
})

// 表单数据
const formData = ref({
  id: null,
  type: 'EXPENSE',
  categoryId: null,
  amount: null,
  date: new Date().toISOString().split('T')[0],
  note: '',
  visible: 'PRIVATE'
})

// 状态
const isEdit = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)

// 导入相关
const importDialogVisible = ref(false)
const importForm = ref({
  source: 'SYSTEM',
  file: null,
  fileName: ''
})
const previewData = ref({
  previewId: '',
  totalCount: 0,
  previewList: []
})
const previewLoading = ref(false)
const importLoading = ref(false)
const uploadRef = ref(null)
const showHelp = ref(false)

// 统计
const stats = computed(() => {
  const income = billList.value
    .filter(item => item.type === 'INCOME')
    .reduce((sum, item) => sum + item.amount, 0)
  const expense = billList.value
    .filter(item => item.type === 'EXPENSE')
    .reduce((sum, item) => sum + item.amount, 0)
  return {
    totalIncome: income,
    totalExpense: expense,
    balance: income - expense
  }
})

// 根据类型筛选分类
const filteredCategoryList = computed(() => {
  return categoryList.value.filter(cat => cat.type === formData.value.type)
})

// 格式化数字
const formatNumber = (num) => {
  return (num || 0).toFixed(2)
}

// Tab 切换
const handleTabChange = () => {
  page.value = 1
  filters.value.userId = null
  fetchBills()
}

// 从账单数据中提取家庭成员列表（去重）
const extractMembersFromBills = (bills) => {
  const memberMap = new Map()
  bills.forEach(bill => {
    if (bill.userId && bill.nickname) {
      memberMap.set(bill.userId, {
        userId: bill.userId,
        nickname: bill.nickname
      })
    }
  })
  return Array.from(memberMap.values())
}

// 获取家庭成员列表（备用，用于初始化）
const fetchMembers = async () => {
  try {
    const res = await getMemberList()
    if (res.code === 200) {
      memberList.value = res.data
    }
  } catch (error) {
    console.error('获取家庭成员失败', error)
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 获取账单列表
const fetchBills = async () => {
  try {
    let params = {
      page: page.value,
      pageSize: pageSize.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      type: filters.value.type,
      categoryId: filters.value.categoryId,
      visible: filters.value.visible,
      minAmount: filters.value.minAmount,
      maxAmount: filters.value.maxAmount,
      orderBy: filters.value.orderBy
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    let res
    if (activeTab.value === 'family') {
      // 家人记录：调用管理员接口
      if (filters.value.userId) {
        params.userId = filters.value.userId
      }
      res = await getFamilyBillList(params)
    } else {
      // 我的记录
      res = await getBillList(params)
    }
    
    if (res.code === 200) {
      billList.value = res.data.list || []
      total.value = res.data.total || 0
      // 家人记录时，从账单数据中提取家庭成员列表
      if (activeTab.value === 'family') {
        memberList.value = extractMembersFromBills(res.data.list || [])
        // 家人记录无数据时显示弹窗提示
        if (!res.data.list || res.data.list.length === 0) {
          ElMessageBox.alert('未查询到家庭成员的收支记录', '提示', {
            confirmButtonText: '确定',
            type: 'info'
          })
        }
      }
    } else {
      // 接口失败时清空列表（比如未加入家庭组、非管理员等情况）
      billList.value = []
      total.value = 0
      // 根据错误码显示弹窗提示
      if (res.code === 400) {
        ElMessageBox.alert(res.message || '当前用户未加入任何家庭组', '提示', {
          confirmButtonText: '确定',
          type: 'info'
        })
      } else if (res.code === 403) {
        ElMessageBox.alert(res.message || '仅家庭管理员可查看成员记录明细', '提示', {
          confirmButtonText: '确定',
          type: 'warning'
        })
      } else {
        ElMessageBox.alert(res.message || '获取数据失败', '提示', {
          confirmButtonText: '确定',
          type: 'error'
        })
      }
    }
  } catch (error) {
    console.error('获取账单失败', error)
    ElMessage.error('获取账单失败')
  }
}

// 搜索
const searchBills = () => {
  page.value = 1
  fetchBills()
}

// 重置筛选
const resetFilters = () => {
  filters.value = {
    userId: null,
    type: '',
    categoryId: '',
    visible: '',
    minAmount: '',
    maxAmount: '',
    orderBy: ''
  }
  dateRange.value = []
  page.value = 1
  fetchBills()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  page.value = 1
  fetchBills()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchBills()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  formData.value = {
    id: null,
    type: 'EXPENSE',
    categoryId: null,
    amount: null,
    date: new Date().toISOString().split('T')[0],
    note: '',
    visible: 'PRIVATE'
  }
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (item) => {
  isEdit.value = true
  formData.value = {
    id: item.id,
    type: item.type,
    categoryId: item.categoryId,
    amount: item.amount,
    date: item.date,
    note: item.note || '',
    visible: item.visible || 'PRIVATE'
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formData.value.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!formData.value.amount || formData.value.amount <= 0) {
    ElMessage.warning('请输入正确的金额')
    return
  }

  submitting.value = true
  try {
    const data = {
      type: formData.value.type,
      categoryId: formData.value.categoryId,
      amount: formData.value.amount,
      date: formData.value.date,
      note: formData.value.note,
      visible: formData.value.visible
    }
    
    let res
    if (isEdit.value) {
      data.id = formData.value.id
      res = await updateBill(data)
    } else {
      res = await addBill(data)       
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchBills()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 确认删除
const confirmDelete = (item) => {
  ElMessageBox.confirm(
    '确定要删除这条收支记录吗？',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteBill(item.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchBills()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {})
}

// 监听类型变化，清空分类选择
watch(() => formData.value.type, () => {
  formData.value.categoryId = null
})

// 打开导入对话框
const openImportDialog = () => {
  previewData.value = { previewId: '', totalCount: 0, previewList: [] }
  importForm.value = { source: 'SYSTEM', file: null, fileName: '' }
  importDialogVisible.value = true
}

// 关闭导入对话框
const closeImportDialog = () => {
  importDialogVisible.value = false
  previewData.value = { previewId: '', totalCount: 0, previewList: [] }
}

// 文件选择后处理
const handleFileChange = async (uploadFile) => {
  const ext = uploadFile.name.split('.').pop().toLowerCase()
  if (ext !== 'xlsx' && ext !== 'csv') {
    ElMessage.error('仅支持 .xlsx 和 .csv 格式文件')
    uploadRef.value.clearFiles()
    return
  }
  importForm.value.file = uploadFile.raw
  importForm.value.fileName = uploadFile.name
  
  if (!importForm.value.source) {
    ElMessage.error('请先选择文件来源')
    uploadRef.value.clearFiles()
    return
  }
  
  previewLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importForm.value.file)
    formData.append('source', importForm.value.source)
    const res = await previewImport(formData)
    if (res.code === 200) {
      previewData.value = {
        previewId: res.data.previewId,
        totalCount: res.data.totalCount,
        previewList: res.data.previewList
      }
    } else {
      ElMessage.error(res.message || '预览失败')
      uploadRef.value.clearFiles()
      importForm.value.fileName = ''
    }
  } catch (error) {
    ElMessage.error('预览失败：' + (error.message || '网络错误'))
    uploadRef.value.clearFiles()
    importForm.value.fileName = ''
  } finally {
    previewLoading.value = false
  }
}

// 确认导入
const handleConfirmImport = async () => {
  if (!previewData.value.previewId) {
    ElMessage.error('请先预览数据')
    return
  }
  importLoading.value = true
  try {
    const res = await confirmImport(previewData.value.previewId)
    if (res.code === 200) {
      const { successCount, failCount, failReasons } = res.data
      let msg = `<span style="color: #27ae60;">导入完成</span>！成功 ${successCount} 条，失败 ${failCount} 条`
      if (failCount > 0) {
        msg += `<br><br><span style="color: #e74c3c;">失败原因</span><br>`
        failReasons.forEach(item => {
          msg += `第 ${item.row} 行: ${item.reason}<br>`
        })
      }
      ElMessageBox.alert(msg, '导入结果', {
        confirmButtonText: '确定',
        type: failCount > 0 ? 'warning' : 'success',
        dangerouslyUseHTMLString: true
      }).then(() => {
        closeImportDialog()
        fetchBills()
      })
    } else {
      ElMessageBox.alert(res.message || '导入失败', '导入失败', {
        confirmButtonText: '确定',
        type: 'error'
      })
    }
  } catch (error) {
    ElMessageBox.alert(error.message || '网络错误，请重试', '导入失败', {
      confirmButtonText: '确定',
      type: 'error'
    })
  } finally {
    importLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchCategories()
  fetchMembers()
  fetchBills()
})
</script>

<style scoped>
.bill-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #409eff;
  padding-left: 16px;
}
.page-header h2 {
  margin: 0 0 8px 0;
}
.page-header p {
  margin: 0;
  color: #909399;
}
.mb-4 {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
.text-success {
  color: #67c23a;
}
.text-danger {
  color: #f56c6c;
}
.text-muted {
  color: #909399;
}
.small {
  font-size: 12px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.button-group {
  display: flex;
  gap: 8px;
}
/* 操作按钮在一行显示 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}
/* 可见范围标签宽度一致 */
.visibility-tag {
  min-width: 100px;
  text-align: center;
}
/* 导入弹窗相对定位 */
:deep(.el-dialog) {
  margin-left: calc(17vw);
}
</style>