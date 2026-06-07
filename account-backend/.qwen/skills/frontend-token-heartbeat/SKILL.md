---
name: frontend-token-heartbeat
description: 前端定时轮询检测登录是否过期（token心跳）—— 已废弃，浏览器节流导致不生效
source: auto-skill
extracted_at: '2026-06-07T15:05:09.991Z'
---

# 前端 Token 心跳检测（定时轮询）—— 已废弃

## ⚠️ 结论：此方案不生效，已删除

实际验证发现：**浏览器对后台标签页的 `setInterval` 和 `fetch` 有节流机制**，当页面处于非活跃状态时，定时器回调会被大幅延迟甚至抑制。因此心跳轮询无法在用户不操作时及时检测到 token 过期。

**最终方案：保持原有的被动检测机制**（用户操作触发 API 请求 → 后端返回 401 → 前端弹窗提示并跳转登录页）。这是最可靠的方式，因为：
- 用户只要有操作就会触发请求，请求经过 Sa-Token 鉴权，过期立即返回 401
- 浏览器不会节流用户主动触发的请求
- 零额外网络开销

## 背景（历史记录）

用户反馈：网页没有定期检查登录是否过期的逻辑，很多时候需要手动刷新页面才会显示"登录已过期"。Sa-Token 的 token 是随机字符串（非 JWT），前端无法解码过期时间，只能通过发请求检测。

## 尝试过的方案及失败原因

### 方案一：`setInterval` + 独立 `axios.get`（不走 request 实例）

在 `request.js` 中用 `setInterval` 每 N 秒发一次 `GET /user/info`，手动带 `Authorization` header，检测到 `code === 401` 时弹窗跳转。

**失败原因：** 浏览器对后台标签页的 `setInterval` 有节流机制，页面不活跃时定时器不触发或大幅延迟。

### 方案二：`setInterval` + `request` 实例（带 `_isHeartbeat` 标记）

心跳请求走 `request` 实例以复用请求拦截器自动带 token，在响应拦截器中通过 `_isHeartbeat` 标记区分心跳请求，避免双重弹窗。

**失败原因：** 同上，`setInterval` 被浏览器节流。且逻辑更复杂（拦截器 + 心跳回调双重判断）。

### 方案三：`setInterval` + `fetch` API（完全绕过 Axios）

用原生 `fetch` 发请求，完全绕过 Axios 拦截器，逻辑自包含。

**失败原因：** 同上，`setInterval` 被浏览器节流。`fetch` 本身在后台标签页也可能被延迟。

## 教训总结

1. **不要依赖 `setInterval` 做后台检测** — 浏览器为了省电和性能，会节流后台标签页的定时器。Chrome 将后台标签页的 `setInterval` 最小间隔限制为 1000ms，且实际触发频率远低于设定值。
2. **`fetch` 在后台标签页也可能被延迟** — 浏览器会将后台标签页的网络请求优先级降低。
3. **被动检测是最可靠的** — 用户操作 → 发请求 → 后端鉴权 → 返回 401 → 前端处理。这个链路不会被浏览器干扰。
4. **如果确实需要主动检测**，可以考虑：
   - 监听 `visibilitychange` 事件，用户切回页面时检查一次
   - 监听 `mousedown` / `touchstart` 事件，用户点击时检查一次
   - 在路由守卫 `beforeEach` 中检查（每次路由切换时发一个轻量请求）
   - 但这些方案仍然依赖用户交互，无法做到"完全不操作也能自动检测"

## 代码清理

删除以下内容：
1. `request.js` 末尾的心跳相关代码（`startHeartbeat`、`stopHeartbeat`、`doHeartbeatCheck`、`storage` 事件监听、初始化调用）
2. `.env` 文件中的 `VITE_TOKEN_CHECK_INTERVAL` 变量
3. 响应拦截器中与 `_isHeartbeat` 相关的条件判断（如果有）
