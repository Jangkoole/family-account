---
name: page-header-style-unification
description: Vue3 + Element Plus 多页面标题风格统一：蓝色左边框 + h1 标题 + 右侧操作按钮
source: auto-skill
extracted_at: '2026-06-07T15:40:27.345Z'
---

# 页面标题风格统一

## 问题

项目中多个页面（收支记录、统计分析、分类管理、个人信息等）的标题风格不一致：有的用 `h2`、有的用 `h1`，有的有左边框、有的没有，字号也不统一。

## 方案：统一的 `page-header` 容器

### 模板结构

```vue
<div class="page-header">
  <div class="header-left">
    <h1>页面标题</h1>
  </div>
  <div class="header-right">
    <el-button type="primary" plain>
      <el-icon><Plus /></el-icon> 操作按钮
    </el-button>
  </div>
</div>
```

### CSS

```css
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin-bottom: 20px;
  min-height: 40px;
}
.page-header h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}
.header-right {
  display: flex;
  gap: 8px;
}
```

### 关键要点

1. **使用 `h1` 而非 `h2`** — 页面级标题应该用一级标题，语义正确。
2. **蓝色左边框** — `border-left: 4px solid #409eff` 是 Element Plus 主题色，视觉上统一。
3. **`min-height: 40px`** — 确保标题区域有最小高度，按钮和标题垂直对齐一致。
4. **`align-items: center`** — 标题和右侧按钮垂直居中对齐。
5. **`margin: 0`** — 去掉 `h1` 默认上下外边距，避免标题区域高度不一致。
6. **`font-size: 20px`** — 统一字号，覆盖 `h1` 浏览器默认字号（约 24px 或 32px）。

### 适用页面

- 收支记录管理（BillView）
- 收支分类管理（CategoryView）
- 汇总统计（StatView）
- 个人信息（ProfileView）— 如果也有标题的话

### 注意事项

- 如果页面标题下方有副标题/描述文字，可以在 `h1` 后面加 `<p>` 标签，样式参考：
  ```css
  .page-header p {
    margin: 0;
    color: #909399;
    font-size: 13px;
  }
  ```
- 如果页面没有右侧操作按钮，可以省略 `header-right`，`page-header` 仍然保持左边框样式。
