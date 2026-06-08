<template>
  <div class="dashboard">
    <div class="page-top">
      <h1 class="handwrite">本月概览</h1>
      <span class="page-date">{{ todayStr }}</span>
    </div>

    <div class="sticky-notes">
      <div class="sticky note-green card-enter">
        <span class="sticky-label">收入</span>
        <span class="sticky-value mono-num">¥{{ formatMoney(monthSummary.totalIncome) }}</span>
      </div>
      <div class="sticky note-red card-enter">
        <span class="sticky-label">支出</span>
        <span class="sticky-value mono-num">¥{{ formatMoney(monthSummary.totalExpense) }}</span>
      </div>
      <div class="sticky note-blue card-enter">
        <span class="sticky-label">结余</span>
        <span class="sticky-value mono-num" :class="balanceClass">¥{{ formatMoney(monthSummary.balance) }}</span>
      </div>
    </div>

    <div v-if="familySummary" class="family-tape card-enter">
      <span class="tape-item mono-num">🏠 家庭收入 <b class="income">¥{{ formatMoney(familySummary.familyTotalIncome) }}</b></span>
      <span class="tape-div">·</span>
      <span class="tape-item mono-num">家庭支出 <b class="expense">¥{{ formatMoney(familySummary.familyTotalExpense) }}</b></span>
      <span class="tape-div">·</span>
      <span class="tape-item mono-num">结余 <b>¥{{ formatMoney(familySummary.familyBalance) }}</b></span>
    </div>

    <div class="charts-row">
      <div class="chart-block card-enter">
        <el-card shadow="never">
          <template #header><span class="handwrite card-title">支出分类</span></template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </div>
      <div class="chart-block card-enter">
        <el-card shadow="never">
          <template #header><span class="handwrite card-title">收支趋势</span></template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </div>
    </div>

    <div class="card-enter">
      <el-card shadow="never">
        <template #header><span class="handwrite card-title">最近记录</span></template>
        <el-table :data="recentBills" v-if="recentBills.length">
          <el-table-column prop="date" label="日期" width="110" />
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="type" label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small">
                {{ row.type === 'INCOME' ? '收入' : '支出' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="row.type === 'INCOME' ? 'income' : 'expense'" class="mono-num">
                {{ row.type === 'INCOME' ? '+' : '−' }}¥{{ formatMoney(row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="120" />
        </el-table>
        <el-empty description="暂无记录" v-else />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { getDashboardSummary } from '../../api/dashboard'
import * as echarts from 'echarts'

const monthSummary = ref({ totalIncome: 0, totalExpense: 0, balance: 0 })
const categoryChart = ref([]); const trendChart = ref([])
const recentBills = ref([]); const familySummary = ref(null)
const pieChartRef = ref(null); const lineChartRef = ref(null)
let pieChartInstance = null; let lineChartInstance = null

const todayStr = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
const balanceClass = computed(() => monthSummary.value.balance >= 0 ? 'income' : 'expense')
function formatMoney(v) { return (v ?? 0).toFixed(2) }

function initCharts() {
  if (pieChartRef.value) {
    pieChartInstance = echarts.init(pieChartRef.value)
    pieChartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      series: [{
        type: 'pie', radius: ['50%', '78%'], center: ['50%', '52%'],
        itemStyle: { borderRadius: 6, borderColor: '#FDF9F2', borderWidth: 3 },
        label: { color: '#3A3428', fontSize: 12, formatter: '{b}\n{d}%' },
        data: categoryChart.value.length ? categoryChart.value.map(i => ({ name: i.categoryName, value: i.amount })) : [{ name: '暂无', value: 1 }]
      }]
    })
  }
  if (lineChartRef.value) {
    lineChartInstance = echarts.init(lineChartRef.value)
    lineChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '支出'], textStyle: { color: '#3A3428' }, top: 0 },
      grid: { top: 36, left: 4, right: 20, bottom: 0, containLabel: true },
      xAxis: { type: 'category', data: trendChart.value.map(i => i.date.slice(5)), axisLine: { lineStyle: { color: '#EBE3D5' } }, axisTick: { show: false }, axisLabel: { color: '#6B6254' } },
      yAxis: { type: 'value', axisLabel: { color: '#6B6254', formatter: '¥{value}' }, splitLine: { lineStyle: { color: '#F5EFE3' } } },
      series: [
        { name: '收入', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, data: trendChart.value.map(i => Number(i.income)), lineStyle: { color: '#5B8C5A', width: 2.5 }, itemStyle: { color: '#5B8C5A' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(91, 140, 90, 0.15)' }, { offset: 1, color: 'rgba(91, 140, 90, 0)' }]) } },
        { name: '支出', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, data: trendChart.value.map(i => Number(i.expense)), lineStyle: { color: '#C06050', width: 2.5 }, itemStyle: { color: '#C06050' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(192, 96, 80, 0.15)' }, { offset: 1, color: 'rgba(192, 96, 80, 0)' }]) } }
      ]
    })
  }
}

function handleResize() { pieChartInstance?.resize(); lineChartInstance?.resize() }

onMounted(async () => {
  try {
    const res = await getDashboardSummary()
    if (res.code === 200) {
      monthSummary.value = res.data.monthSummary; categoryChart.value = res.data.categoryChart
      trendChart.value = res.data.trendChart; recentBills.value = res.data.recentBills
      familySummary.value = res.data.familySummary
    }
  } catch (e) { console.error(e) }
  await nextTick(); initCharts(); window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); pieChartInstance?.dispose(); lineChartInstance?.dispose() })
</script>

<style scoped>
.dashboard { width: 100%; }
.page-top { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 28px; }
.page-top h1 { font-size: 30px; margin: 0; }
.page-date { font-size: 13px; color: var(--ink-faded); }
.sticky-notes { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 24px; }
.sticky { padding: 24px 20px; border-radius: 4px 16px 4px 16px; border: 2px solid var(--paper-dark); background: var(--paper-warm); box-shadow: var(--shadow-md); transition: all var(--transition-smooth); }
.sticky:hover { box-shadow: var(--shadow-lg); transform: rotate(-0.5deg) translateY(-2px); }
.note-green { border-left: 5px solid var(--tag-green); }
.note-red { border-left: 5px solid var(--tag-red); }
.note-blue { border-left: 5px solid var(--tag-blue); }
.sticky-label { display: block; font-family: var(--font-display); font-size: 16px; color: var(--ink-faded); margin-bottom: 8px; }
.sticky-value { font-size: 28px; font-weight: 700; color: var(--ink); }
.income { color: var(--income); }
.expense { color: var(--expense); }
.family-tape { display: flex; align-items: center; gap: 12px; padding: 14px 24px; background: var(--paper-cream); border-radius: 6px 18px 6px 18px; border: 2px dashed var(--paper-dark); margin-bottom: 24px; font-size: 14px; }
.tape-div { color: var(--ink-faded); }
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.chart-box { width: 100%; height: 350px; }
.card-title { font-size: 18px !important; }
@media (max-width: 768px) { .sticky-notes, .charts-row { grid-template-columns: 1fr; } }
</style>
