<template>
  <div class="stat-container">
    <h2>汇总统计</h2>

    <el-card class="filter-card">
      <div class="filter-row">
        <el-select v-model="timeType" style="width: 120px" @change="onQuery">
          <el-option label="按日" value="DAY" />
          <el-option label="按周" value="WEEK" />
          <el-option label="按月" value="MONTH" />
          <el-option label="按年" value="YEAR" />
        </el-select>
        <el-date-picker
          v-model="queryDate"
          :type="datePickerType"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          @change="onQuery"
        />
      </div>
    </el-card>

    <div class="summary-cards">
      <el-card class="card income">
        <div class="card-label">总收入</div>
        <div class="card-value">¥{{ summaryData.totalIncome }}</div>
      </el-card>
      <el-card class="card expense">
        <div class="card-label">总支出</div>
        <div class="card-value">¥{{ summaryData.totalExpense }}</div>
      </el-card>
      <el-card class="card balance" :class="summaryData.balance >= 0 ? 'positive' : 'negative'">
        <div class="card-label">结余</div>
        <div class="card-value">¥{{ summaryData.balance }}</div>
      </el-card>
    </div>

    <div class="charts-row">
      <el-card class="chart-card">
        <template #header>
          <div class="chart-header">
            <span>分类占比</span>
            <el-radio-group v-model="categoryType" size="small" @change="onCategoryTypeChange">
              <el-radio-button value="EXPENSE">支出</el-radio-button>
              <el-radio-button value="INCOME">收入</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="pieChartRef" class="chart-box"></div>
        <div v-if="pieEmpty" class="chart-empty">暂无数据</div>
      </el-card>
      <el-card class="chart-card">
        <template #header>收支趋势</template>
        <div ref="trendChartRef" class="chart-box"></div>
        <div v-if="trendEmpty" class="chart-empty">暂无数据</div>
      </el-card>
    </div>

    <el-card v-if="familyData" class="family-card">
      <template #header>家庭成员统计</template>
      <div class="family-summary">
        <span>家庭总收入：<b>¥{{ familyData.familyTotalIncome }}</b></span>
        <span>家庭总支出：<b>¥{{ familyData.familyTotalExpense }}</b></span>
        <span>家庭结余：<b>¥{{ familyData.familyBalance }}</b></span>
      </div>
      <el-table :data="familyData.members" stripe>
        <el-table-column prop="nickname" label="成员" />
        <el-table-column prop="totalIncome" label="收入" />
        <el-table-column prop="totalExpense" label="支出" />
        <el-table-column prop="balance" label="结余" />
        <el-table-column prop="incomePercentage" label="收入占比">
          <template #default="{ row }">{{ row.incomePercentage }}%</template>
        </el-table-column>
        <el-table-column prop="expensePercentage" label="支出占比">
          <template #default="{ row }">{{ row.expensePercentage }}%</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getSummary, getCategoryStats, getTrend, getFamilyMemberStats } from '@/api/stat'

const timeType = ref('MONTH')
const queryDate = ref(new Date().toISOString().slice(0, 10))
const categoryType = ref('EXPENSE')

const datePickerType = computed(() => {
  const map = { DAY: 'date', WEEK: 'week', MONTH: 'month', YEAR: 'year' }
  return map[timeType.value] || 'month'
})

const summaryData = reactive({ totalIncome: '0.00', totalExpense: '0.00', balance: '0.00' })

const pieChartRef = ref(null)
const trendChartRef = ref(null)
let pieChart = null
let trendChart = null

const pieEmpty = ref(true)
const trendEmpty = ref(true)
const familyData = ref(null)

function getDateRange() {
  const d = new Date(queryDate.value + 'T00:00:00')
  const fmt = date => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  let start, end
  switch (timeType.value) {
    case 'DAY':
      start = end = d
      break
    case 'WEEK': {
      const dayOfWeek = d.getDay()
      const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek
      start = new Date(d); start.setDate(d.getDate() + mondayOffset)
      end = new Date(start); end.setDate(start.getDate() + 6)
      break
    }
    case 'MONTH':
      start = new Date(d.getFullYear(), d.getMonth(), 1)
      end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
      break
    case 'YEAR':
      start = new Date(d.getFullYear(), 0, 1)
      end = new Date(d.getFullYear(), 11, 31)
      break
    default:
      start = end = d
  }
  return { start: fmt(start), end: fmt(end) }
}

function ensurePieChart() {
  if (!pieChart && pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

function ensureTrendChart() {
  if (!trendChart && trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
}

async function loadCategoryChart(start, end) {
  const ct = categoryType.value
  const res = await getCategoryStats(ct, start, end)
  await nextTick()
  ensurePieChart()
  if (res.data?.list?.length && pieChart) {
    pieEmpty.value = false
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '55%'], data: res.data.list.map(item => ({ name: item.categoryName, value: item.amount })) }]
    }, true)
  } else {
    pieEmpty.value = true
    pieChart?.clear()
  }
}

function onCategoryTypeChange() {
  const { start, end } = getDateRange()
  loadCategoryChart(start, end)
}

async function loadTrendChart(start, end) {
  const gran = timeType.value === 'YEAR' ? 'MONTH' : 'DAY'
  const res = await getTrend(gran, start, end)
  ensureTrendChart()
  if (res.data?.length && trendChart) {
    trendEmpty.value = false
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '支出'] },
      xAxis: { type: 'category', data: res.data.map(d => d.date) },
      yAxis: { type: 'value' },
      series: [
        { name: '收入', type: 'line', data: res.data.map(d => d.income), smooth: true, color: '#67C23A' },
        { name: '支出', type: 'line', data: res.data.map(d => d.expense), smooth: true, color: '#F56C6C' }
      ]
    }, true)
  } else {
    trendEmpty.value = true
    trendChart?.clear()
  }
}

async function onQuery() {
  const { start, end } = getDateRange()

  const sumRes = await getSummary(timeType.value, queryDate.value)
  Object.assign(summaryData, sumRes.data)

  await Promise.all([
    loadCategoryChart(start, end),
    loadTrendChart(start, end)
  ])

  try {
    const famRes = await getFamilyMemberStats(start, end)
    familyData.value = famRes.data
  } catch {
    familyData.value = null
  }
}

onMounted(async () => {
  await nextTick()
  ensurePieChart()
  ensureTrendChart()
  onQuery()
})

window.addEventListener('resize', () => {
  pieChart?.resize()
  trendChart?.resize()
})
</script>

<style scoped>
.stat-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
.filter-card { margin-bottom: 20px; }
.filter-row { display: flex; gap: 12px; align-items: center; }

.summary-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.card { text-align: center; }
.card-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.card-value { font-size: 28px; font-weight: bold; }
.card.income .card-value { color: #67C23A; }
.card.expense .card-value { color: #F56C6C; }
.card.balance.positive .card-value { color: #409EFF; }
.card.balance.negative .card-value { color: #F56C6C; }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; }
.chart-box { width: 100%; height: 350px; }
.chart-empty { display: flex; align-items: center; justify-content: center; height: 350px; color: #909399; }

.family-card { margin-bottom: 20px; }
.family-summary { display: flex; gap: 24px; margin-bottom: 16px; font-size: 15px; }
</style>
