<template>
  <div class="dashboard">
    <!-- 本月概览卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="income-card">
          <div class="card-content">
            <div class="card-label">本月收入</div>
            <div class="card-value income">¥{{ formatMoney(monthSummary.totalIncome) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="expense-card">
          <div class="card-content">
            <div class="card-label">本月支出</div>
            <div class="card-value expense">¥{{ formatMoney(monthSummary.totalExpense) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="balance-card">
          <div class="card-content">
            <div class="card-label">本月结余</div>
            <div class="card-value" :class="balanceClass">¥{{ formatMoney(monthSummary.balance) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 家庭概况（仅家庭成员可见） -->
    <el-row :gutter="20" class="family-summary" v-if="familySummary">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">家庭本月概况</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="family-stat-item">
                <div class="stat-label">家庭总收入</div>
                <div class="stat-value income">¥{{ formatMoney(familySummary.familyTotalIncome) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="family-stat-item">
                <div class="stat-label">家庭总支出</div>
                <div class="stat-value expense">¥{{ formatMoney(familySummary.familyTotalExpense) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="family-stat-item">
                <div class="stat-label">家庭结余</div>
                <div class="stat-value" :class="familyBalanceClass">¥{{ formatMoney(familySummary.familyBalance) }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 分类支出占比饼图 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">本月支出分类占比</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 最近一周收支趋势折线图 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">最近一周收支趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近收支记录 -->
    <el-row :gutter="20" class="recent-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="section-title">最近收支记录</span>
          </template>
          <el-table :data="recentBills" stripe style="width: 100%" v-if="recentBills.length > 0">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small">
                  {{ row.type === 'INCOME' ? '收入' : '支出' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                <span :class="row.type === 'INCOME' ? 'income' : 'expense'">
                  {{ row.type === 'INCOME' ? '+' : '-' }}¥{{ formatMoney(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注" />
          </el-table>
          <el-empty description="暂无收支记录" v-else />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { getDashboardSummary } from '../api/dashboard'
import { useUserStore } from '../stores/user'
import * as echarts from 'echarts'

const userStore = useUserStore()

const monthSummary = ref({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0
})
const categoryChart = ref([])
const trendChart = ref([])
const recentBills = ref([])
const familySummary = ref(null)

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChartInstance = null
let lineChartInstance = null

const balanceClass = computed(() => {
  const b = monthSummary.value.balance
  if (b > 0) return 'income'
  if (b < 0) return 'expense'
  return ''
})

const familyBalanceClass = computed(() => {
  if (!familySummary.value) return ''
  const b = familySummary.value.familyBalance
  if (b > 0) return 'income'
  if (b < 0) return 'expense'
  return ''
})

function formatMoney(value) {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

function initCharts() {
  // 饼图
  if (pieChartRef.value) {
    pieChartInstance = echarts.init(pieChartRef.value)
    const pieData = categoryChart.value.length > 0
      ? categoryChart.value.map(item => ({
          name: item.categoryName,
          value: item.amount
        }))
      : [{ name: '暂无数据', value: 1 }]

    pieChartInstance.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: ¥{c} ({d}%)'
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: pieData
      }]
    })
  }

  // 折线图
  if (lineChartRef.value) {
    lineChartInstance = echarts.init(lineChartRef.value)
    const dates = trendChart.value.map(item => item.date.slice(5))
    const incomes = trendChart.value.map(item => Number(item.income))
    const expenses = trendChart.value.map(item => Number(item.expense))

    lineChartInstance.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['收入', '支出']
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [
        {
          name: '收入',
          type: 'line',
          smooth: true,
          data: incomes,
          itemStyle: { color: '#67C23A' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
            ])
          }
        },
        {
          name: '支出',
          type: 'line',
          smooth: true,
          data: expenses,
          itemStyle: { color: '#F56C6C' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
              { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
            ])
          }
        }
      ]
    })
  }
}

function handleResize() {
  pieChartInstance?.resize()
  lineChartInstance?.resize()
}

onMounted(async () => {
  try {
    const res = await getDashboardSummary()
    if (res.code === 200) {
      monthSummary.value = res.data.monthSummary
      categoryChart.value = res.data.categoryChart
      trendChart.value = res.data.trendChart
      recentBills.value = res.data.recentBills
      familySummary.value = res.data.familySummary
    }
  } catch (e) {
    console.error('获取仪表盘数据失败', e)
  }

  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChartInstance?.dispose()
  lineChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.summary-cards {
  margin-bottom: 20px;
}

.card-content {
  text-align: center;
  padding: 10px 0;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
}

.income {
  color: #67C23A;
}

.expense {
  color: #F56C6C;
}

.income-card {
  border-top: 3px solid #67C23A;
}

.expense-card {
  border-top: 3px solid #F56C6C;
}

.balance-card {
  border-top: 3px solid #409EFF;
}

.family-summary {
  margin-bottom: 20px;
}

.family-stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.recent-row {
  margin-bottom: 20px;
}
</style>
