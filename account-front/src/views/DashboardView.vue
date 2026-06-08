<template>
  <div class="dashboard">

    <!-- 页面标题 -->
    <div class="page-header-zh">
      <h1>首页概览</h1>
    </div>

    <!-- 本月概览卡片 -->
    <div class="summary-cards">
      <div class="summary-card card-income card-enter">
        <div class="card-decoration"></div>
        <div class="card-inner">
          <div class="card-label">本月收入</div>
          <div class="card-value income">¥{{ formatMoney(monthSummary.totalIncome) }}</div>
        </div>
      </div>
      <div class="summary-card card-expense card-enter">
        <div class="card-decoration"></div>
        <div class="card-inner">
          <div class="card-label">本月支出</div>
          <div class="card-value expense">¥{{ formatMoney(monthSummary.totalExpense) }}</div>
        </div>
      </div>
      <div class="summary-card card-balance card-enter">
        <div class="card-decoration"></div>
        <div class="card-inner">
          <div class="card-label">本月结余</div>
          <div class="card-value" :class="balanceClass">¥{{ formatMoney(monthSummary.balance) }}</div>
        </div>
      </div>
    </div>

    <!-- 家庭概况 -->
    <div v-if="familySummary" class="family-section card-enter">
      <el-card shadow="never" class="zh-card">
        <template #header>
          <div class="card-header-zh">
            <span class="header-dot"></span>
            <span>家庭本月概况</span>
          </div>
        </template>
        <div class="family-stats">
          <div class="family-stat-item">
            <span class="fs-label">家庭总收入</span>
            <span class="fs-value income">¥{{ formatMoney(familySummary.familyTotalIncome) }}</span>
          </div>
          <div class="family-stat-divider"></div>
          <div class="family-stat-item">
            <span class="fs-label">家庭总支出</span>
            <span class="fs-value expense">¥{{ formatMoney(familySummary.familyTotalExpense) }}</span>
          </div>
          <div class="family-stat-divider"></div>
          <div class="family-stat-item">
            <span class="fs-label">家庭结余</span>
            <span class="fs-value" :class="familyBalanceClass">¥{{ formatMoney(familySummary.familyBalance) }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card shadow="never" class="zh-card chart-card card-enter">
        <template #header>
          <div class="card-header-zh">
            <span class="header-dot"></span>
            <span>本月支出分类占比</span>
          </div>
        </template>
        <div ref="pieChartRef" class="chart-container"></div>
      </el-card>

      <el-card shadow="never" class="zh-card chart-card card-enter">
        <template #header>
          <div class="card-header-zh">
            <span class="header-dot"></span>
            <span>最近一周收支趋势</span>
          </div>
        </template>
        <div ref="lineChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 最近收支记录 -->
    <el-card shadow="never" class="zh-card card-enter">
      <template #header>
        <div class="card-header-zh">
          <span class="header-dot"></span>
          <span>最近收支记录</span>
        </div>
      </template>
      <el-table :data="recentBills" stripe style="width: 100%" v-if="recentBills.length > 0">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small" effect="light">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'INCOME' ? 'income' : 'expense'" class="amount-display">
              {{ row.type === 'INCOME' ? '+' : '-' }}¥{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="150" />
      </el-table>
      <el-empty description="暂无收支记录" v-else />
    </el-card>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { getDashboardSummary } from '../api/dashboard'
import { useUserStore } from '../stores/user'
import * as echarts from 'echarts'

const userStore = useUserStore()

const monthSummary = ref({ totalIncome: 0, totalExpense: 0, balance: 0 })
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
  return familySummary.value.familyBalance >= 0 ? 'income' : 'expense'
})

function formatMoney(value) {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

/* 新中式 ECharts 配色 */
const ZH_COLORS = [
  '#C4342E', '#B8860B', '#4A7C59', '#8B7D6B',
  '#D4544E', '#C9A96E', '#6B9E7A', '#A89880',
  '#E0706A', '#D4B87A', '#8DB59A', '#C4B5A0'
]

function initCharts() {
  // 饼图
  if (pieChartRef.value) {
    pieChartInstance = echarts.init(pieChartRef.value)
    const pieData = categoryChart.value.length > 0
      ? categoryChart.value.map(item => ({
          name: item.categoryName,
          value: item.amount
        }))
      : [{ name: '暂无数据', value: 1, itemStyle: { color: '#E8D5B7' } }]

    pieChartInstance.setOption({
      color: ZH_COLORS,
      tooltip: {
        trigger: 'item',
        formatter: '{b}: ¥{c} ({d}%)',
        backgroundColor: '#fffdf8',
        borderColor: '#E8D5B7',
        textStyle: { color: '#2c2416', fontFamily: 'Noto Sans SC' }
      },
      series: [{
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['50%', '55%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fffdf8',
          borderWidth: 3
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontFamily: 'Noto Sans SC',
          fontSize: 11,
          color: '#3d3226'
        },
        emphasis: {
          label: { fontSize: 14, fontWeight: 'bold' },
          scaleSize: 8
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
        trigger: 'axis',
        backgroundColor: '#fffdf8',
        borderColor: '#E8D5B7',
        textStyle: { color: '#2c2416', fontFamily: 'Noto Sans SC' }
      },
      legend: {
        data: ['收入', '支出'],
        textStyle: { fontFamily: 'Noto Sans SC', color: '#3d3226' },
        top: 0
      },
      grid: { top: 40, left: 10, right: 20, bottom: 0, containLabel: true },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#E8D5B7' } },
        axisTick: { show: false },
        axisLabel: { color: '#8B7D6B', fontFamily: 'Noto Sans SC' }
      },
      yAxis: {
        type: 'value',
        axisLabel: { formatter: '¥{value}', color: '#8B7D6B', fontFamily: 'Noto Sans SC' },
        splitLine: { lineStyle: { color: '#F0EBE0' } }
      },
      series: [
        {
          name: '收入',
          type: 'line',
          smooth: true,
          data: incomes,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#4A7C59', width: 2.5 },
          itemStyle: { color: '#4A7C59' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(74, 124, 89, 0.2)' },
              { offset: 1, color: 'rgba(74, 124, 89, 0.02)' }
            ])
          }
        },
        {
          name: '支出',
          type: 'line',
          smooth: true,
          data: expenses,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#C4342E', width: 2.5 },
          itemStyle: { color: '#C4342E' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(196, 52, 46, 0.2)' },
              { offset: 1, color: 'rgba(196, 52, 46, 0.02)' }
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

/* ---- 概览卡片 ---- */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.summary-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
  padding: 28px 24px;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
  cursor: default;
}

.summary-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.summary-card::after {
  content: '';
  position: absolute;
  right: -20px;
  top: -20px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  opacity: 0.08;
  pointer-events: none;
}

.card-income::after { background: #4A7C59; }
.card-expense::after { background: #C4342E; }
.card-balance::after { background: #B8860B; }

/* 顶部彩色条 */
.card-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.card-income .card-decoration {
  background: linear-gradient(90deg, #4A7C59, #6B9E7A);
}
.card-expense .card-decoration {
  background: linear-gradient(90deg, #C4342E, #D4544E);
}
.card-balance .card-decoration {
  background: linear-gradient(90deg, #B8860B, #C9A96E);
}

.card-inner {
  text-align: center;
  position: relative;
  z-index: 1;
}

.card-label {
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-secondary);
  letter-spacing: 3px;
  margin-bottom: 12px;
}

.card-value {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
}

.income { color: #4A7C59; }
.expense { color: #C4342E; }

/* ---- 中式卡片 ---- */
.zh-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
}

.card-header-zh {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-dot {
  width: 8px;
  height: 8px;
  background: var(--cinnabar);
  border-radius: 50%;
  flex-shrink: 0;
}

/* ---- 家庭概况 ---- */
.family-section {
  margin-bottom: 24px;
}

.family-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px 0;
}

.family-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.fs-label {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 2px;
}

.fs-value {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
}

.family-stat-divider {
  width: 1px;
  height: 40px;
  background: var(--gold-pale);
}

/* ---- 图表 ---- */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  min-height: 420px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

/* 响应式 */
@media (max-width: 768px) {
  .summary-cards,
  .charts-row {
    grid-template-columns: 1fr;
  }
  .family-stats {
    flex-direction: column;
    gap: 16px;
  }
  .family-stat-divider {
    width: 60%;
    height: 1px;
  }
}
</style>
