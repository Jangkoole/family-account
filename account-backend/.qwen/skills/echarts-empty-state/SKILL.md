---
name: echarts-empty-state
description: Vue3 + ECharts 图表空状态处理：v-if/v-else 切换 + 按需 init/dispose，避免卡片高度不一致
source: auto-skill
extracted_at: '2026-06-07T15:12:25.260Z'
---

# ECharts 图表空状态处理

## 问题

ECharts 图表在有数据和无数据时，卡片内容区高度不一致。无数据时如果只显示"暂无数据"文字，卡片会显得很空；如果保留空的 chart 容器，ECharts canvas 仍然占据空间，导致卡片高度膨胀。

## 方案：`v-if` / `v-else` 互斥切换 + 按需 `init` / `dispose`

### 模板结构

```vue
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
  <!-- v-if / v-else 互斥，不同时存在 -->
  <div ref="pieChartRef" v-if="!pieEmpty" class="chart-box"></div>
  <div v-else class="chart-empty">暂无数据</div>
</el-card>
```

### CSS

```css
.chart-box { width: 100%; height: 350px; }
.chart-empty { display: flex; align-items: center; justify-content: center; height: 350px; color: #909399; }
```

两个容器高度一致（如 350px），确保卡片内容区高度不变。

### JS 逻辑：按需 init / dispose

```javascript
let pieChart = null

async function loadChart() {
  const res = await getChartData()
  if (res.code === 200 && res.data?.length) {
    // 有数据：显示 chart-box，初始化 ECharts
    pieEmpty.value = false
    await nextTick()  // 等待 DOM 渲染
    if (pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
      pieChart.setOption({ /* chart options */ }, true)
    }
  } else {
    // 无数据：显示 chart-empty，销毁 ECharts 实例
    pieEmpty.value = true
    pieChart?.dispose()
    pieChart = null
  }
}
```

### 为什么不用 `v-show`？

`v-show` 用 `display: none` 隐藏元素，但 ECharts 的 `resize()` 在窗口 resize 时检测到隐藏容器的宽高为 0，会把图表尺寸重置为 0。再次显示时图表被压扁。

### 为什么不用 `display: none` / `height: 0` 的 CSS 方案？

- `display: none`：同 `v-show`，ECharts resize 时容器尺寸为 0
- `height: 0`：ECharts 检测到容器高度为 0，图表被压缩到不可见

### 为什么不用 `clear()` 代替 `dispose()`？

`clear()` 只清除图表内容，不销毁实例。如果 DOM 被 `v-if` 移除，实例仍然持有对已移除 DOM 的引用，可能导致内存泄漏。`dispose()` 彻底销毁实例，配合 `v-if` 的 DOM 重建，每次显示时重新 `init`，确保图表尺寸正确。

### 完整生命周期

| 状态变化 | 操作 |
|---------|------|
| 无数据 → 有数据 | `pieEmpty = false` → DOM 渲染 `chart-box` → `nextTick` → `echarts.init` → `setOption` |
| 有数据 → 无数据 | `pieEmpty = true` → `chart.dispose()` → `chart = null` → DOM 切换为 `chart-empty` |
| 组件卸载 | 无需额外处理，`v-if` 自动移除 DOM |
| 窗口 resize | `chart?.resize()` — 只在有实例时调用，无数据时 `chart` 为 null，安全跳过 |

### 注意事项

1. **`nextTick` 必须调用两次**：第一次等 `pieEmpty` 响应式更新触发 DOM 重新渲染，第二次等 `chart-box` 元素挂载到 DOM 后获取 `offsetWidth/Height`
2. **`dispose` 后必须置为 `null`**：防止后续代码误调用已销毁的实例
3. **`resize` 事件监听中加空值判断**：`chart?.resize()`，避免无数据时报错
4. **如果图表在弹窗/折叠面板中**：需要等元素可见后再 `init`，否则宽高为 0
