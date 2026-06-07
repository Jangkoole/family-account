---
name: element-plus-button-custom-color
description: 使用 CSS 变量自定义 Element Plus plain 按钮颜色，保留组件 hover/active 状态
source: auto-skill
extracted_at: '2026-06-07T15:40:27.345Z'
---

# Element Plus Plain 按钮颜色自定义

## 问题

Element Plus 的 `type="success"` 按钮默认绿色太亮（`#67C23A`），白色文字在亮色背景上可读性差。使用 `plain` 属性后背景变浅，但文字和边框色仍然是默认的亮绿色，视觉上仍然刺眼。

## 方案：CSS 变量覆盖

Element Plus 按钮组件支持通过 CSS 变量自定义颜色，无需覆盖组件样式或写额外 CSS 类。

### 基础用法

```vue
<el-button type="success" plain style="--el-button-text-color: #3d7a22; --el-button-border-color: #3d7a22; --el-button-bg-color: #e8f5e0;">
  <el-icon><Plus /></el-icon> 按钮文字
</el-button>
```

### 可覆盖的 CSS 变量

| 变量名 | 作用 | 示例值 |
|--------|------|--------|
| `--el-button-text-color` | 文字颜色（普通状态） | `#3d7a22` |
| `--el-button-border-color` | 边框颜色（普通状态） | `#3d7a22` |
| `--el-button-bg-color` | 背景颜色（普通状态） | `#e8f5e0` |
| `--el-button-hover-text-color` | 悬停时文字颜色 | 不设置则自动加深 |
| `--el-button-hover-bg-color` | 悬停时背景颜色 | 不设置则自动加深 |
| `--el-button-hover-border-color` | 悬停时边框颜色 | 不设置则自动加深 |
| `--el-button-active-text-color` | 点击时文字颜色 | 不设置则自动加深 |
| `--el-button-active-bg-color` | 点击时背景颜色 | 不设置则自动加深 |
| `--el-button-active-border-color` | 点击时边框颜色 | 不设置则自动加深 |

### 注意事项

1. **必须保留 `type` 属性** — CSS 变量只在对应 `type` 的按钮上生效。去掉 `type` 后 CSS 变量不生效。
2. **建议同时设置三个变量** — `text-color`、`border-color`、`bg-color` 一起设置，确保颜色协调。
3. **hover/active 状态自动处理** — Element Plus 会根据普通状态的颜色自动计算加深后的 hover/active 颜色，无需手动设置。
4. **`plain` 属性建议保留** — `plain` 模式背景色较浅，文字和边框颜色突出，更适合自定义颜色场景。
5. **内联 style 即可** — 不需要额外 CSS 类或 scoped style，内联 style 优先级足够覆盖组件默认变量。

### 颜色参考

| 用途 | 文字/边框色 | 背景色 | 效果 |
|------|------------|--------|------|
| 柔和绿色 | `#3d7a22` | `#e8f5e0` | 深绿文字+浅绿底，不刺眼 |
| 柔和红色 | `#c03636` | `#fef0f0` | 深红文字+浅红底 |
| 柔和蓝色 | `#2a6bb0` | `#ecf5ff` | 深蓝文字+浅蓝底 |
| 柔和橙色 | `#b8821a` | `#fdf6ec` | 深橙文字+浅橙底 |
