---
name: element-plus-icon-availability
description: Element Plus @element-plus/icons-vue 中不存在的图标（CircleFilled）及替代方案
source: auto-skill
extracted_at: '2026-06-07T15:46:35.848Z'
---

# Element Plus 图标可用性参考

## 问题

在 Vue 3 + Element Plus 项目中使用 `@element-plus/icons-vue` 的图标时，某些图标名不存在，导致 `SyntaxError: The requested module does not provide an export named 'xxx'`，页面完全无法加载。

## 已知不存在的图标

| 图标名 | 错误信息 | 替代方案 |
|--------|---------|---------|
| `CircleFilled` | `does not provide an export named 'CircleFilled'` | 使用 `CircleCheck`（实心圆+勾）或 `CircleClose`（实心圆+叉） |

## 排查方法

当遇到 `does not provide an export named 'xxx'` 错误时：

1. **检查官方文档** — 访问 Element Plus 图标文档页面确认图标名
2. **检查 node_modules** — 查看 `node_modules/@element-plus/icons-vue/dist/types/index.d.ts` 或 `global.d.ts` 中的导出列表
3. **使用已知存在的图标** — 以下图标经过验证存在：
   - `Plus`、`Delete`、`Edit`、`Search`、`Refresh`
   - `CircleCheck`、`CircleClose`、`CirclePlus`
   - `ArrowDown`、`ArrowUp`、`ArrowLeft`、`ArrowRight`
   - `Download`、`Upload`、`UploadFilled`
   - `User`、`UserFilled`、`Avatar`
   - `Setting`、`Tools`
   - `InfoFilled`、`WarningFilled`、`SuccessFilled`、`CircleCheckFilled`

## 注意事项

- `CircleFilled` 不存在，但 `CircleCheck` 和 `CircleClose` 存在。命名上 `CircleCheck` 是实心圆+勾，`CircleCheckFilled` 也是实心圆+勾（可能样式略有不同）。
- 图标导入错误会导致整个页面白屏（Vite 编译时抛出 `SyntaxError`），因为 `import` 是静态的。
- 如果只是临时使用，可以用 `el-icon` + SVG 或 Unicode 字符替代，避免导入不存在的图标名。
