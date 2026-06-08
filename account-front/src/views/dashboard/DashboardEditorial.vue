<template>
  <div class="dashboard">

    <!-- 大标题 -->
    <div class="ed-hed">
      <h1 class="ed-headline">本月概览</h1>
      <span class="ed-dek">{{ todayStr }}</span>
    </div>
    <div class="ed-rule"></div>

    <!-- 核心数字 -->
    <div class="ed-numbers">
      <div class="ed-num-block card-enter">
        <span class="ed-num-kicker">本月收入</span>
        <span class="ed-num-value income editorial-num">¥{{ formatMoney(monthSummary.totalIncome) }}</span>
      </div>
      <div class="ed-num-block card-enter">
        <span class="ed-num-kicker">本月支出</span>
        <span class="ed-num-value expense editorial-num">¥{{ formatMoney(monthSummary.totalExpense) }}</span>
      </div>
      <div class="ed-num-block card-enter">
        <span class="ed-num-kicker">本月结余</span>
        <span class="ed-num-value editorial-num" :class="balanceClass">¥{{ formatMoney(monthSummary.balance) }}</span>
      </div>
    </div>

    <div class="ed-rule"></div>

    <!-- 家庭概况 -->
    <div v-if="familySummary" class="ed-family card-enter">
      <span class="ed-section-label">家庭</span>
      <div class="ed-family-row">
        <span class="ed-num-kicker">总收入</span>
        <span class="editorial-num income" style="font-size: 28px">¥{{ formatMoney(familySummary.familyTotalIncome) }}</span>
        <span class="ed-num-kicker" style="margin-left: 40px">总支出</span>
        <span class="editorial-num expense" style="font-size: 28px">¥{{ formatMoney(familySummary.familyTotalExpense) }}</span>
        <span class="ed-num-kicker" style="margin-left: 40px">结余</span>
        <span class="editorial-num" style="font-size: 28px">¥{{ formatMoney(familySummary.familyBalance) }}</span>
      </div>
    </div>

    <div class="ed-rule"></div>

    <!-- 图表区 -->
    <div class="ed-charts">
      <div class="ed-chart card-enter">
        <span class="ed-section-label">支出分类占比</span>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
      <div class="ed-chart card-enter">
        <span class="ed-section-label">收支趋势</span>
        <div ref="lineChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="ed-rule"></div>

    <!-- 最近记录 -->
    <div class="card-enter">
      <span class="ed-section-label">最近收支记录</span>
      <el-table :data="recentBills" v-if="recentBills.length">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column prop="type" label="" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'INCOME' ? 'income' : 'expense'" class="editorial-num">
              {{ row.type === 'INCOME' ? '+' : '−' }}¥{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="150" />
      </el-table>
      <el-empty description="暂无收支记录" v-else />
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
        type: 'pie', radius: ['55%', '82%'], center: ['50%', '52%'],
        itemStyle: { borderRadius: 0, borderColor: '#FCFCFA', borderWidth: 3 },
        label: { color: '#444', fontSize: 12, formatter: '{b}\n{d}%' },
        data: categoryChart.value.length ? categoryChart.value.map(i => ({ name: i.categoryName, value: i.amount })) : [{ name: '暂无', value: 1 }]
      }]
    })
  }
  if (lineChartRef.value) {
    lineChartInstance = echarts.init(lineChartRef.value)
    lineChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '支出'], textStyle: { color: '#444', fontFamily: 'DM Sans' }, top: 0 },
      grid: { top: 36, left: 4, right: 20, bottom: 0, containLabel: true },
      xAxis: { type: 'category', data: trendChart.value.map(i => i.date.slice(5)), axisLine: { lineStyle: { color: '#E8E6E0' } }, axisTick: { show: false }, axisLabel: { color: '#888', fontFamily: 'DM Sans' } },
      yAxis: { type: 'value', axisLabel: { color: '#888', formatter: '¥{value}', fontFamily: 'DM Sans' }, splitLine: { lineStyle: { color: '#F7F6F3' } } },
      series: [
        { name: '收入', type: 'line', smooth: false, symbol: 'circle', symbolSize: 5, data: trendChart.value.map(i => Number(i.income)), lineStyle: { color: '#3A7D44', width: 2 }, itemStyle: { color: '#3A7D44' } },
        { name: '支出', type: 'line', smooth: false, symbol: 'circle', symbolSize: 5, data: trendChart.value.map(i => Number(i.expense)), lineStyle: { color: '#C04040', width: 2 }, itemStyle: { color: '#C04040' } }
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
.ed-hed { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 16px; }
.ed-headline { font-size: 48px; margin: 0; font-weight: 800; }
.ed-dek { font-size: 14px; color: var(--ink-light); font-style: italic; }
.ed-rule { height: 1px; background: var(--rule); margin: 32px 0; }
.ed-numbers { display: grid; grid-template-columns: repeat(3, 1fr); gap: 40px; }
.ed-num-block { display: flex; flex-direction: column; gap: 8px; }
.ed-num-kicker { font-family: var(--font-body); font-size: 11px; color: var(--ink-light); text-transform: uppercase; letter-spacing: 0.12em; font-weight: 500; }
.ed-num-value { font-size: 44px; font-weight: 700; line-height: 1.1; }
.income { color: var(--income); }
.expense { color: var(--expense); }
.ed-section-label { font-family: var(--font-display); font-size: 18px; font-weight: 600; color: var(--ink); display: block; margin-bottom: 20px; }
.ed-family-row { display: flex; align-items: baseline; gap: 12px; flex-wrap: wrap; }
.ed-charts { display: grid; grid-template-columns: 1fr 1fr; gap: 48px; }
.chart-box { width: 100%; height: 380px; }
@media (max-width: 768px) {
  .ed-headline { font-size: 32px; }
  .ed-numbers { grid-template-columns: 1fr; gap: 24px; }
  .ed-charts { grid-template-columns: 1fr; }
  .ed-num-value { font-size: 36px; }
}
</style>
