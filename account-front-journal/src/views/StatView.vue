<template>
  <div class="stat-container">
    <div class="page-header-zh">
      <h1>汇总统计</h1>
    </div>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="zh-card filter-card">
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
        <el-radio-group v-model="scope" @change="onQuery">
          <el-radio-button value="PERSONAL">个人</el-radio-button>
          <el-radio-button value="FAMILY">家庭</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <!-- 汇总卡片 -->
    <div v-if="!notInFamily" class="summary-cards">
      <div class="summary-card card-income">
        <div class="sc-label">{{ scope === 'FAMILY' ? '家庭总收入' : '总收入' }}</div>
        <div class="sc-value income">¥{{ summaryData.totalIncome }}</div>
      </div>
      <div class="summary-card card-expense">
        <div class="sc-label">{{ scope === 'FAMILY' ? '家庭总支出' : '总支出' }}</div>
        <div class="sc-value expense">¥{{ summaryData.totalExpense }}</div>
      </div>
      <div class="summary-card card-balance" :class="summaryData.balance >= 0 ? 'positive' : 'negative'">
        <div class="sc-label">结余</div>
        <div class="sc-value" :class="summaryData.balance >= 0 ? 'income' : 'expense'">¥{{ summaryData.balance }}</div>
      </div>
    </div>

    <!-- 图表 -->
    <div v-if="!notInFamily" class="charts-row">
      <el-card shadow="never" class="zh-card chart-card">
        <template #header>
          <div class="chart-header-zh">
            <div class="ch-left">
              <span class="header-dot"></span>
              <span>分类占比</span>
            </div>
            <el-radio-group v-model="categoryType" size="small" @change="onCategoryTypeChange">
              <el-radio-button value="EXPENSE">支出</el-radio-button>
              <el-radio-button value="INCOME">收入</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="pieChartRef" v-if="!pieEmpty" class="chart-box"></div>
        <div v-else class="chart-empty">暂无数据</div>
      </el-card>

      <el-card shadow="never" class="zh-card chart-card">
        <template #header>
          <div class="ch-left">
            <span class="header-dot"></span>
            <span>收支趋势</span>
          </div>
        </template>
        <div ref="trendChartRef" v-if="!trendEmpty" class="chart-box"></div>
        <div v-else class="chart-empty">暂无数据</div>
      </el-card>
    </div>

    <!-- 家庭统计 -->
    <el-card v-if="notInFamily" shadow="never" class="zh-card family-card">
      <template #header>
        <div class="ch-left">
          <span class="header-dot"></span>
          <span>家庭成员统计</span>
        </div>
      </template>
      <div class="no-family-tip">
        <div class="tip-seal">!</div>
        <div class="tip-text">您尚未加入任何家庭组</div>
        <div class="tip-hint">请先创建家庭或通过邀请码加入家庭，才能查看家庭统计数据</div>
      </div>
    </el-card>

    <el-card v-else-if="familyData" shadow="never" class="zh-card family-card">
      <template #header>
        <div class="ch-left">
          <span class="header-dot"></span>
          <span>家庭成员统计</span>
        </div>
      </template>
      <div class="family-summary">
        <span>家庭总收入：<b class="income">¥{{ familyData.familyTotalIncome }}</b></span>
        <span class="summary-divider">|</span>
        <span>家庭总支出：<b class="expense">¥{{ familyData.familyTotalExpense }}</b></span>
        <span class="summary-divider">|</span>
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
const scope = ref('PERSONAL')

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
const notInFamily = ref(false)

const ZH_COLORS = ['#C4342E', '#B8860B', '#4A7C59', '#8B7D6B', '#D4544E', '#C9A96E', '#6B9E7A', '#A89880']

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
    case 'DAY': start = end = d; break
    case 'WEEK': {
      const dayOfWeek = d.getDay()
      const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek
      start = new Date(d); start.setDate(d.getDate() + mondayOffset)
      end = new Date(start); end.setDate(start.getDate() + 6)
      break
    }
    case 'MONTH': start = new Date(d.getFullYear(), d.getMonth(), 1); end = new Date(d.getFullYear(), d.getMonth() + 1, 0); break
    case 'YEAR': start = new Date(d.getFullYear(), 0, 1); end = new Date(d.getFullYear(), 11, 31); break
    default: start = end = d
  }
  return { start: fmt(start), end: fmt(end) }
}

async function loadCategoryChart(start, end) {
  const ct = categoryType.value
  const res = await getCategoryStats(ct, start, end, scope.value)
  await nextTick()
  if (res.code === 200 && res.data?.list?.length) {
    pieEmpty.value = false
    await nextTick()
    if (pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
      pieChart.setOption({
        color: ZH_COLORS,
        tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)', backgroundColor: '#fffdf8', borderColor: '#E8D5B7', textStyle: { color: '#2c2416' } },
        series: [{
          type: 'pie', radius: ['40%', '70%'], center: ['50%', '55%'],
          itemStyle: { borderRadius: 4, borderColor: '#fffdf8', borderWidth: 3 },
          label: { formatter: '{b}\n{d}%', fontSize: 11, color: '#3d3226' },
          data: res.data.list.map(item => ({ name: item.categoryName, value: item.amount }))
        }]
      }, true)
    }
  } else { pieEmpty.value = true; pieChart?.dispose(); pieChart = null }
}

function onCategoryTypeChange() {
  const { start, end } = getDateRange()
  loadCategoryChart(start, end)
}

async function loadTrendChart(start, end) {
  const gran = timeType.value === 'YEAR' ? 'MONTH' : 'DAY'
  const res = await getTrend(gran, start, end, scope.value)
  if (res.code === 200 && res.data?.length) {
    trendEmpty.value = false
    await nextTick()
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value)
      trendChart.setOption({
        tooltip: { trigger: 'axis', backgroundColor: '#fffdf8', borderColor: '#E8D5B7', textStyle: { color: '#2c2416' } },
        legend: { data: ['收入', '支出'], textStyle: { color: '#3d3226' }, top: 0 },
        grid: { top: 40, left: 10, right: 20, bottom: 0, containLabel: true },
        xAxis: { type: 'category', data: res.data.map(d => d.date), axisLine: { lineStyle: { color: '#E8D5B7' } }, axisTick: { show: false }, axisLabel: { color: '#8B7D6B' } },
        yAxis: { type: 'value', axisLabel: { color: '#8B7D6B' }, splitLine: { lineStyle: { color: '#F0EBE0' } } },
        series: [
          { name: '收入', type: 'line', data: res.data.map(d => d.income), smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: '#4A7C59', width: 2.5 }, itemStyle: { color: '#4A7C59' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(74, 124, 89, 0.2)' }, { offset: 1, color: 'rgba(74, 124, 89, 0.02)' }]) } },
          { name: '支出', type: 'line', data: res.data.map(d => d.expense), smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: '#C4342E', width: 2.5 }, itemStyle: { color: '#C4342E' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(196, 52, 46, 0.2)' }, { offset: 1, color: 'rgba(196, 52, 46, 0.02)' }]) } }
        ]
      }, true)
    }
  } else { trendEmpty.value = true; trendChart?.dispose(); trendChart = null }
}

async function onQuery() {
  const { start, end } = getDateRange()
  notInFamily.value = false
  const sumRes = await getSummary(timeType.value, queryDate.value, scope.value)
  if (sumRes.code !== 200) {
    if (sumRes.message?.includes('未加入家庭')) {
      notInFamily.value = true; summaryData.totalIncome = '0.00'; summaryData.totalExpense = '0.00'; summaryData.balance = '0.00'
      pieEmpty.value = true; trendEmpty.value = true
      pieChart?.dispose(); pieChart = null; trendChart?.dispose(); trendChart = null; familyData.value = null
    }
    return
  }
  Object.assign(summaryData, sumRes.data)
  await Promise.all([loadCategoryChart(start, end), loadTrendChart(start, end)])
  if (scope.value === 'FAMILY') {
    const famRes = await getFamilyMemberStats(start, end)
    if (famRes.code === 200) familyData.value = famRes.data
    else familyData.value = null
  }
}

onMounted(async () => { await nextTick(); onQuery() })

window.addEventListener('resize', () => { pieChart?.resize(); trendChart?.resize() })
</script>

<style scoped>
.stat-container { width: 100%; }

.page-header-zh {
  display: flex;
  align-items: center;
  gap: 12px;
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
.page-header-zh::before {
  content: '';
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, var(--cinnabar) 0%, var(--gold) 100%);
  border-radius: 2px;
}

.zh-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
}
.filter-card { margin-bottom: 20px; }
.filter-card .el-card__body { padding: 20px; }
.filter-row { display: flex; gap: 12px; align-items: center; }

/* 汇总卡片 */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.summary-card {
  background: #fffdf8;
  border: 1px solid var(--gold-pale);
  border-radius: var(--radius-md);
  padding: 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
}
.summary-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}
.sc-label {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 2px;
  margin-bottom: 10px;
}
.sc-value {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
}
.income { color: #4A7C59; }
.expense { color: #C4342E; }

/* 图表 */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}
.chart-card { min-height: 420px; }
.chart-box { width: 100%; height: 350px; }
.chart-empty {
  display: flex; align-items: center; justify-content: center;
  height: 350px; color: var(--text-secondary); font-size: 14px;
}
.chart-header-zh {
  display: flex; justify-content: space-between; align-items: center;
}
.ch-left {
  display: flex; align-items: center; gap: 10px;
}
.header-dot {
  width: 8px; height: 8px; background: var(--cinnabar); border-radius: 50%; flex-shrink: 0;
}

/* 家庭统计 */
.family-card { margin-bottom: 20px; }
.family-summary {
  display: flex; gap: 16px; align-items: center; margin-bottom: 16px;
  font-size: 15px; color: var(--text-regular);
}
.summary-divider { color: var(--gold-pale); }

.no-family-tip {
  display: flex; flex-direction: column; align-items: center; padding: 40px; text-align: center;
}
.tip-seal {
  width: 48px; height: 48px; border: 2px solid #B8860B; border-radius: 3px;
  display: flex; align-items: center; justify-content: center;
  color: #B8860B; font-family: var(--font-display); font-weight: 900; font-size: 22px;
  transform: rotate(-6deg); margin-bottom: 16px;
}
.tip-text { font-size: 16px; color: var(--text-regular); margin-bottom: 8px; }
.tip-hint { font-size: 14px; color: var(--text-secondary); }

@media (max-width: 768px) {
  .summary-cards, .charts-row { grid-template-columns: 1fr; }
  .family-summary { flex-direction: column; align-items: flex-start; }
}
</style>
